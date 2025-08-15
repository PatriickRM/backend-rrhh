package com.rrhh.backend.application.exception;

public class ErrorSistema extends RuntimeException{
    public ErrorSistema(String message){
        super(message);
    }
}
