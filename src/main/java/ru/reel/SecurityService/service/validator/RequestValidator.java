package ru.reel.SecurityService.service.validator;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.reel.request.error.RequestError;
import ru.reel.request.error.reason.ErrorReason;

@Component
public class RequestValidator {
    public ResponseEntity<RequestError> checkBodyEmpty(Object body) {
        if(body == null || (body instanceof String && ((String) body).isEmpty()))
            return ResponseEntity.badRequest().body(RequestError.builder()
                    .errorReason(ErrorReason.EMPTY)
                    .message("body")
                    .build());
        return null;
    }
}
