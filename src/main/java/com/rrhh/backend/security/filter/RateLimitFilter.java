package com.rrhh.backend.security.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Filtro de rate limiting para el endpoint de autenticación.
 *
 * Protege contra ataques de fuerza bruta usando el algoritmo Token Bucket (Bucket4j):
 * - Máximo 5 intentos de login por IP cada 5 minutos
 * - Los tokens se recargan gradualmente (1 token/minuto)
 * - Si se agotan los tokens, responde 429 Too Many Requests
 * - El mapa de buckets se limpia automáticamente (ConcurrentHashMap en memoria)
 */
@Slf4j
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String LOGIN_PATH   = "/api/auth/login";
    private static final int    MAX_REQUESTS = 5;           // máx intentos
    private static final long   WINDOW_MINUTES = 5L;        // ventana de tiempo

    // Mapa IP → Bucket (en memoria — válido para instancia única)
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Solo aplica al endpoint de login
        if (!LOGIN_PATH.equals(request.getServletPath())) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = extractClientIp(request);
        Bucket bucket = buckets.computeIfAbsent(clientIp, this::createNewBucket);

        if (bucket.tryConsume(1)) {
            // Token consumido — dejar pasar la request
            long remainingTokens = bucket.getAvailableTokens();
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(remainingTokens));
            filterChain.doFilter(request, response);
        } else {
            // Sin tokens — responder 429
            log.warn("Rate limit alcanzado para IP: {} en {}", clientIp, LOGIN_PATH);
            sendRateLimitResponse(response, clientIp);
        }
    }

    private Bucket createNewBucket(String ip) {
        // 5 tokens máx, recarga de 1 token por minuto (5 en total cada 5 minutos)
        Bandwidth limit = Bandwidth.classic(
                MAX_REQUESTS,
                Refill.greedy(MAX_REQUESTS, Duration.ofMinutes(WINDOW_MINUTES))
        );
        return Bucket.builder().addLimit(limit).build();
    }

    private void sendRateLimitResponse(HttpServletResponse response, String ip) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.addHeader("Retry-After", String.valueOf(WINDOW_MINUTES * 60));
        response.getWriter().write(String.format("""
                {
                  "error": "Demasiados intentos de inicio de sesión.",
                  "mensaje": "Has superado el límite de %d intentos. Espera %d minutos antes de intentarlo de nuevo.",
                  "retryAfterSeconds": %d
                }
                """, MAX_REQUESTS, WINDOW_MINUTES, WINDOW_MINUTES * 60));
    }

    /**
     * Extrae la IP real del cliente, considerando proxies y load balancers.
     * Prioriza X-Forwarded-For, luego X-Real-IP, y finalmente remoteAddr.
     */
    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            // X-Forwarded-For puede tener múltiples IPs: "client, proxy1, proxy2"
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp.trim();
        }
        return request.getRemoteAddr();
    }
}
