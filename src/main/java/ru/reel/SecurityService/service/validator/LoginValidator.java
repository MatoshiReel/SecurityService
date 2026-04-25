package ru.reel.SecurityService.service.validator;

import org.springframework.stereotype.Component;
import ru.reel.SecurityService.dto.NewLoginDto;
import ru.reel.SecurityService.repository.AccountRepository;
import ru.reel.request.error.RequestFieldError;
import ru.reel.request.error.reason.ErrorReason;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
public class LoginValidator implements Validator<RequestFieldError, List<RequestFieldError>, NewLoginDto> {
    public static final int LOGIN_MIN_SIZE = 3;
    public static final int LOGIN_MAX_SIZE = 20;
    public static final String ALLOW_LOGIN_CHARS = "a-z A-Z 0-9 _";
    private final PasswordValidator passwordValidator;
    private final AccountRepository repository;

    public LoginValidator(PasswordValidator passwordValidator, AccountRepository repository) {
        this.passwordValidator = passwordValidator;
        this.repository = repository;
    }

    public void setAccountId(UUID accountId) {
        this.passwordValidator.setAccountId(accountId);
    }

    @Override
    public List<RequestFieldError> validate(NewLoginDto dto) {
        List<RequestFieldError> errors = new ArrayList<>();
        errors.add(passwordValidator.checkPasswordNullable(dto.password, "password"));
        errors.add(checkLoginNullable(dto.newLogin, "newLogin"));
        errors = new ArrayList<>(errors.stream().filter(Objects::nonNull).toList());
        if(!errors.isEmpty())
            return errors;
        errors.add(passwordValidator.checkPasswordMatching(dto.password, "password"));
        errors.add(checkNewLoginSize(dto.newLogin));
        errors.add(checkNewLoginPatternMatching(dto.newLogin));
        errors.add(checkLoginExisting(dto.newLogin, "newLogin"));
        return errors.stream().filter(Objects::nonNull).toList();
    }

    public RequestFieldError checkLoginNullable(String login, String loginFieldName) {
        if(isEmpty(login))
            return RequestFieldError.builder().field(loginFieldName).errorReason(ErrorReason.EMPTY).message(loginFieldName).build();
        return null;
    }

    public boolean isEmpty(String text) {
        return text == null || text.isEmpty();
    }

    public RequestFieldError checkNewLoginSize(String newLogin) {
        if(isLoginSizeLtMin(newLogin))
            return RequestFieldError.builder().field("newLogin").errorReason(ErrorReason.LESS_SIZE).message("newLogin", String.valueOf(LOGIN_MIN_SIZE)).build();
        if(isLoginSizeGtMax(newLogin))
            return RequestFieldError.builder().field("newLogin").errorReason(ErrorReason.GREATER_SIZE).message("newLogin", String.valueOf(LOGIN_MAX_SIZE)).build();
        return null;
    }

    public boolean isLoginSizeLtMin(String login) {
        return login.length() < LOGIN_MIN_SIZE;
    }

    public boolean isLoginSizeGtMax(String login) {
        return login.length() > LOGIN_MAX_SIZE;
    }

    public RequestFieldError checkNewLoginPatternMatching(String newLogin) {
        if(isLoginPatternNotValid(newLogin))
            return RequestFieldError.builder().field("newLogin").errorReason(ErrorReason.PATTERN).message("newLogin", ALLOW_LOGIN_CHARS).build();
        return null;
    }

    public boolean isLoginPatternNotValid(String login) {
        return !login.matches("^\\w+$");
    }

    public RequestFieldError checkLoginExisting(String login, String loginFieldName) {
        if(isLoginExisting(login))
            return RequestFieldError.builder().field(loginFieldName).errorReason(ErrorReason.EXIST).message(loginFieldName).build();
        return null;
    }

    public boolean isLoginExisting(String login) {
        return repository.findByLogin(login).isPresent();
    }
}
