package ru.reel.SecurityService.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.reel.request.error.RequestError;
import ru.reel.request.error.RequestParamError;
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

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<RequestParamError> handleInvalidParamTypeError(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.badRequest().body(RequestParamError
                .builder()
                .param(e.getParameter().getParameterName())
                .errorReason(ErrorReason.BAD_DATA_TYPE)
                .customMessage(e.getMessage())
                .build());
    }
}
