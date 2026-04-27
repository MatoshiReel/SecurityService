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
import ru.reel.SecurityService.port.mail.MailOtpSender;
import ru.reel.SecurityService.port.mail.MailTokenSender;
import ru.reel.SecurityService.port.redis.EmailOtpTemplate;
import ru.reel.SecurityService.port.redis.EmailTokenTemplate;
import ru.reel.SecurityService.service.AccountService;
import ru.reel.SecurityService.service.otp.OtpGenerator;
import ru.reel.SecurityService.service.token.TokenGenerator;
import ru.reel.SecurityService.service.validator.EmailValidator;
import ru.reel.SecurityService.service.validator.LoginValidator;
import ru.reel.SecurityService.service.validator.PasswordValidator;
import ru.reel.SecurityService.service.validator.RequestValidator;
import ru.reel.request.error.RequestError;
import ru.reel.request.error.RequestFieldError;
import ru.reel.request.error.RequestParamError;
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
    private final OtpGenerator otpGenerator;
    private final MailTokenSender tokenSender;
    private final MailNotificationSender<ModifiedParameter> notificationSender;
    private final MailOtpSender otpSender;
    private final EmailTokenTemplate tokenTemplate;
    private final EmailOtpTemplate otpTemplate;
    private static final short OTP_SIZE = 6;
    private static final short TOKEN_MAX_ATTEMPTS = 3;
    private static final short OTP_MAX_ATTEMPTS = 5;

    public SecurityController(RequestValidator requestValidator, PasswordValidator passwordValidator, LoginValidator loginValidator, EmailValidator emailValidator, AccountService service, TokenGenerator tokenGenerator, OtpGenerator otpGenerator, MailTokenSender tokenSender, MailNotificationSender<ModifiedParameter> notificationSender, MailOtpSender otpSender, EmailTokenTemplate tokenTemplate, EmailOtpTemplate otpTemplate) {
        this.requestValidator = requestValidator;
        this.passwordValidator = passwordValidator;
        this.loginValidator = loginValidator;
        this.emailValidator = emailValidator;
        this.service = service;
        this.tokenGenerator = tokenGenerator;
        this.otpGenerator = otpGenerator;
        this.tokenSender = tokenSender;
        this.notificationSender = notificationSender;
        this.otpSender = otpSender;
        this.tokenTemplate = tokenTemplate;
        this.otpTemplate = otpTemplate;
    }

    @PostMapping(path = "/password/change", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
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

    @PostMapping(path = "/login/change", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
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

    @PostMapping(path = "/email/change", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
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

    @PostMapping(path = "/email/send/token", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> sendTokenToEmail(@RequestBody(required = false) NewEmailDto newEmailDto, @RequestHeader("X-Account-Id") String accountId) throws NoSuchAlgorithmException {
        ResponseEntity<RequestError> emptyBodyResponse = requestValidator.checkBodyEmpty(newEmailDto);
        if(emptyBodyResponse != null)
            return emptyBodyResponse;
        emailValidator.setAccountId(UUID.fromString(accountId));
        List<RequestFieldError> errors = emailValidator.validate(newEmailDto);
        if(!errors.isEmpty())
            return ResponseEntity.badRequest().body(errors);
        if(tokenTemplate.getToken(accountId) != null)
            return ResponseEntity.badRequest().body(RequestError.builder()
                    .errorReason(ErrorReason.EXIST)
                    .message("token")
                    .build());
        String email = service.getEmailById(accountId);
        if(email != null)
            notificationSender.send(email, ModifiedParameter.EMAIL);
        String token = tokenGenerator.generate();
        tokenSender.send(newEmailDto.newEmail, token);
        tokenTemplate.setWithAttempts(accountId, token, TOKEN_MAX_ATTEMPTS);
        service.updatePendingEmailById(newEmailDto.newEmail, accountId);
        service.updateEmailVerifiedById(false, accountId);
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.TEXT_PLAIN).body(tokenTemplate.getTokenTtl().toMinutes());
    }

    @PostMapping(path = "/email/send/otp", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> sendOtpToEmail(@RequestBody(required = false) String password, @RequestHeader("X-Account-Id") String accountId) {
        ResponseEntity<RequestError> emptyBodyResponse = requestValidator.checkBodyEmpty(password);
        if(emptyBodyResponse != null)
            return emptyBodyResponse;
        passwordValidator.setAccountId(UUID.fromString(accountId));
        RequestFieldError error = passwordValidator.checkPasswordMatching(password, "password");
        if(error != null)
            return ResponseEntity.badRequest().body(error);
        String email = service.getEmailById(accountId);
        if(email == null && !service.isEmailVerifiedById(accountId))
            return ResponseEntity.badRequest().body(RequestError.builder()
                    .errorReason(ErrorReason.NOT_EXIST)
                    .message("email")
                    .build());
        String otp = otpGenerator.generate(OTP_SIZE);
        otpSender.send(email, otp);
        otpTemplate.setWithAttempts(email, otp, OTP_MAX_ATTEMPTS);
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.TEXT_PLAIN).body(otpTemplate.getCodeTtl().toMinutes());
    }

    @PostMapping(path = "/email/2fa", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> switch2FaWithEmail(@RequestBody(required = false) String code, @RequestParam(value = "enable", required = false) Boolean isEnable, @RequestHeader("X-Account-Id") String accountId) {
        ResponseEntity<RequestError> emptyBodyResponse = requestValidator.checkBodyEmpty(code);
        if(emptyBodyResponse != null)
            return emptyBodyResponse;
        if(isEnable == null)
            return ResponseEntity.badRequest().body(RequestParamError.builder()
                    .param("enable")
                    .errorReason(ErrorReason.EMPTY)
                    .message("enable")
                    .build());
        String email = service.getEmailById(accountId);
        if(email == null && !service.isEmailVerifiedById(accountId))
            return ResponseEntity.badRequest().body(RequestError.builder()
                    .errorReason(ErrorReason.NOT_EXIST)
                    .message("email")
                    .build());
        if(otpTemplate.getCode(email) == null)
            return ResponseEntity.badRequest().body(RequestError.builder()
                    .errorReason(ErrorReason.NOT_EXIST)
                    .message("code")
                    .build());
        if(otpTemplate.getAttempts(email) <= 0) {
            otpTemplate.delCodeWithAttempts(email);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if(!otpTemplate.verify(code, email)) {
            otpTemplate.decrementAttempts(email);
            return ResponseEntity.badRequest().body(RequestError.builder()
                    .errorReason(ErrorReason.NOT_MATCH)
                    .message("code")
                    .build());
        }
        otpTemplate.delCodeWithAttempts(email);
        service.update2FaEnabledById(isEnable, accountId);
        return ResponseEntity.noContent().build();
    }
}
