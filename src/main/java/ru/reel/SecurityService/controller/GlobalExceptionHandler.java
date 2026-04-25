package ru.reel.SecurityService.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.reel.request.error.RequestError;
import ru.reel.request.error.reason.ErrorReason;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<RequestError> handleBadEncryptionError(IllegalStateException e) {
        return  ResponseEntity.badRequest().body(RequestError.builder()
                .errorReason(ErrorReason.ENCRYPTION)
                .message()
                .build());
    }
}
