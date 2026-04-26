package ru.reel.SecurityService.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.reel.SecurityService.dto.NewEmailDto;
import ru.reel.SecurityService.dto.NewLoginDto;
import ru.reel.SecurityService.dto.NewPasswordDto;
import ru.reel.SecurityService.infrastructure.mail.ModifiedParameter;
import ru.reel.SecurityService.port.mail.MailNotificationSender;
import ru.reel.SecurityService.port.mail.MailTokenSender;
import ru.reel.SecurityService.port.redis.EmailTokenTemplate;
import ru.reel.SecurityService.service.AccountService;
import ru.reel.SecurityService.service.token.TokenGenerator;
import ru.reel.SecurityService.service.validator.EmailValidator;
import ru.reel.SecurityService.service.validator.LoginValidator;
import ru.reel.SecurityService.service.validator.PasswordValidator;
import ru.reel.SecurityService.service.validator.RequestValidator;
import ru.reel.request.error.RequestError;
import ru.reel.request.error.RequestFieldError;
import ru.reel.request.error.reason.ErrorReason;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/security")
public class SecurityController {
    private final RequestValidator requestValidator;
    private final PasswordValidator passwordValidator;
    private final LoginValidator loginValidator;
    private final EmailValidator emailValidator;
    private final AccountService service;
    private final TokenGenerator tokenGenerator;
    private final MailTokenSender tokenSender;
    private final MailNotificationSender notificationSender;
    private final EmailTokenTemplate tokenTemplate;

    public SecurityController(RequestValidator requestValidator, PasswordValidator passwordValidator, LoginValidator loginValidator, EmailValidator emailValidator, AccountService service, TokenGenerator tokenGenerator, MailTokenSender tokenSender, MailNotificationSender<ModifiedParameter> notificationSender, EmailTokenTemplate tokenTemplate) {
        this.requestValidator = requestValidator;
        this.passwordValidator = passwordValidator;
        this.loginValidator = loginValidator;
        this.emailValidator = emailValidator;
        this.service = service;
        this.tokenGenerator = tokenGenerator;
        this.tokenSender = tokenSender;
        this.notificationSender = notificationSender;
        this.tokenTemplate = tokenTemplate;
    }

    @PostMapping(path = "/password/change", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> changePassword(@RequestBody(required = false) NewPasswordDto newPasswordDto, @RequestHeader("X-Account-Id") String accountId) {
        ResponseEntity<RequestError> emptyBodyResponse = requestValidator.checkBodyEmpty(newPasswordDto);
        if(emptyBodyResponse != null)
            return emptyBodyResponse;
        passwordValidator.setAccountId(UUID.fromString(accountId));
        List<RequestFieldError> errors = passwordValidator.validate(newPasswordDto);
        if(!errors.isEmpty())
            return ResponseEntity.badRequest().body(errors);
        service.updatePasswordById(newPasswordDto.newPassword, accountId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/login/change", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> changeLogin(@RequestBody(required = false) NewLoginDto newLoginDto, @RequestHeader("X-Account-Id") String accountId) {
        ResponseEntity<RequestError> emptyBodyResponse = requestValidator.checkBodyEmpty(newLoginDto);
        if(emptyBodyResponse != null)
            return emptyBodyResponse;
        loginValidator.setAccountId(UUID.fromString(accountId));
        List<RequestFieldError> errors = loginValidator.validate(newLoginDto);
        if(!errors.isEmpty())
            return ResponseEntity.badRequest().body(errors);
        service.updateLoginById(newLoginDto.newLogin, accountId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/email/send/token", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> sendTokenToEmail(@RequestBody(required = false) NewEmailDto newEmailDto, @RequestHeader("X-Account-Id") String accountId) throws NoSuchAlgorithmException {
        ResponseEntity<RequestError> emptyBodyResponse = requestValidator.checkBodyEmpty(newEmailDto);
        if(emptyBodyResponse != null)
            return emptyBodyResponse;
        emailValidator.setAccountId(UUID.fromString(accountId));
        List<RequestFieldError> errors = emailValidator.validate(newEmailDto);
        if(!errors.isEmpty())
            return ResponseEntity.badRequest().body(errors);
        if(tokenTemplate.getToken(accountId) == null) {
            String email = service.getEmailById(accountId);
            if(email != null)
                notificationSender.send(email, ModifiedParameter.EMAIL);
            String token = tokenGenerator.generate();
            tokenSender.send(newEmailDto.newEmail, token);
            tokenTemplate.setWithAttempts(accountId, token, (short)3);
            service.updatePendingEmailById(newEmailDto.newEmail, accountId);
            service.updateEmailVerifiedById(false, accountId);
        } else {
            return ResponseEntity.badRequest().body(RequestError.builder()
                    .errorReason(ErrorReason.EXIST)
                    .message("token")
                    .build());
        }
        return ResponseEntity.ok(tokenTemplate.getTokenTtl().toMinutes());
    }

    @PostMapping(path = "/email/change")
    public ResponseEntity<?> changeEmail(@RequestBody(required = false) String token, @RequestHeader("X-Account-Id") String accountId) {
        ResponseEntity<RequestError> emptyBodyResponse = requestValidator.checkBodyEmpty(token);
        if(emptyBodyResponse != null)
            return emptyBodyResponse;
        RequestFieldError error = emailValidator.checkEmailExisting(service.getPendingEmailById(accountId), "email");
        if(error != null)
            return ResponseEntity.badRequest().body(error);
        if(tokenTemplate.getToken(accountId) == null)
            return ResponseEntity.badRequest().body(RequestError.builder()
                    .errorReason(ErrorReason.NOT_EXIST)
                    .message("token")
                    .build());
        if(tokenTemplate.getAttempts(accountId) <= 0) {
            tokenTemplate.delTokenWithAttempts(accountId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if(!tokenTemplate.verify(token, accountId)) {
            tokenTemplate.decrementAttempts(accountId);
            return ResponseEntity.badRequest().body(RequestError.builder()
                    .errorReason(ErrorReason.NOT_MATCH)
                    .message("token")
                    .build());
        }
        tokenTemplate.delTokenWithAttempts(accountId);
        service.updateEmailById(service.getPendingEmailById(accountId), accountId);
        service.updatePendingEmailById(null, accountId);
        service.updateEmailVerifiedById(true, accountId);
        return ResponseEntity.noContent().build();
    }
}
