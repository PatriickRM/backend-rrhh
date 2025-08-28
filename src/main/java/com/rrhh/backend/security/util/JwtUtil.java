package com.rrhh.backend.security.util;

import com.rrhh.backend.application.exception.ErrorSistema;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import io.jsonwebtoken.security.SignatureException;

@Component
public class JwtUtil {
    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.expiration}")
    private Long expirationMs;

    private Key getSigninKey(){
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String generateToken(UserDetails userDetails){
        Map<String,Object> claims = Map.of("roles",userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).toList());
        return Jwts.builder().setClaims(claims).setSubject(userDetails.getUsername()).setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigninKey(), SignatureAlgorithm.HS256).compact();
    }
    public Claims extractAllClaims(String token){
        try {
            return Jwts.parserBuilder().setSigningKey(getSigninKey()).build().parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            throw new ErrorSistema("Token expirado");
        } catch (MalformedJwtException e) {
            throw new ErrorSistema("Token malformado");
        } catch (SignatureException e) {
            throw new ErrorSistema("Firma del token inválida");
        } catch (Exception e) {
            throw new ErrorSistema("Token inválido: " + e.getMessage());
        }
    }
    public String extractUsername(String token){
        return extractAllClaims(token).getSubject();
    }

    public Date extractExpiration(String token){
        return extractAllClaims(token).getExpiration();
    }

    public boolean validateToken(String token, UserDetails user){
        String username = extractUsername(token);
        Date expiration = extractExpiration(token);

        return username.equals(user.getUsername()) && expiration.after(new Date());
    }

}
