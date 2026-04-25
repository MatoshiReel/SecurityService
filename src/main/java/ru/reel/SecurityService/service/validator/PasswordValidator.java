package ru.reel.SecurityService.service.validator;

import lombok.Setter;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.reel.SecurityService.dto.NewPasswordDto;
import ru.reel.SecurityService.entity.Account;
import ru.reel.SecurityService.repository.AccountRepository;
import ru.reel.request.error.RequestFieldError;
import ru.reel.request.error.reason.ErrorReason;

import java.util.*;

@Component
public class PasswordValidator implements Validator<RequestFieldError, List<RequestFieldError>, NewPasswordDto> {
    public static final int PASSWORD_MIN_SIZE = 6;
    public static final int PASSWORD_MAX_SIZE = 24;
    public static final String ALLOW_PASSWORD_CHARS = "a-z A-Z 0-9 _ ! @ # $ % ^ & * ( ) - + =";
    private final AccountRepository repository;
    private final Argon2PasswordEncoder encoder;
    @Setter
    private UUID accountId;

    public PasswordValidator(AccountRepository repository, Argon2PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    @Override
    public List<RequestFieldError> validate(NewPasswordDto dto) throws NullPointerException {
        List<RequestFieldError> errors = new ArrayList<>();
        errors.add(checkPasswordNullable(dto.oldPassword, "oldPassword"));
        errors.add(checkPasswordNullable(dto.newPassword, "newPassword"));
        errors.add(checkPasswordNullable(dto.repeatedPassword, "repeatedPassword"));
        errors = new ArrayList<>(errors.stream().filter(Objects::nonNull).toList());
        if(!errors.isEmpty())
            return errors;
        errors.add(checkNewPasswordSize(dto.newPassword));
        errors.add(checkNewPasswordPatternMatching(dto.newPassword));
        errors.add(checkRepeatedPasswordEqualing(dto.newPassword, dto.repeatedPassword));
        errors.add(checkPasswordMatching(dto.oldPassword, "oldPassword"));
        return errors.stream().filter(Objects::nonNull).toList();
    }

    public RequestFieldError checkPasswordNullable(String password, String passwordFieldName) {
        if(isEmpty(password))
            return RequestFieldError.builder().field(passwordFieldName).errorReason(ErrorReason.EMPTY).message(passwordFieldName).build();
        return null;
    }

    public boolean isEmpty(String text) {
        return text == null || text.isEmpty();
    }

    public RequestFieldError checkNewPasswordSize(String newPassword) {
        if(isPasswordSizeLtMin(newPassword))
            return RequestFieldError.builder().field("newPassword").errorReason(ErrorReason.LESS_SIZE).message("newPassword", String.valueOf(PASSWORD_MIN_SIZE)).build();
        if(isPasswordSizeGtMax(newPassword))
            return RequestFieldError.builder().field("newPassword").errorReason(ErrorReason.GREATER_SIZE).message("newPassword", String.valueOf(PASSWORD_MAX_SIZE)).build();
        return null;
    }

    public boolean isPasswordSizeGtMax(String password) {
        return password.length() > PASSWORD_MAX_SIZE;
    }

    public boolean isPasswordSizeLtMin(String password) {
        return password.length() < PASSWORD_MIN_SIZE;
    }

    public RequestFieldError checkNewPasswordPatternMatching(String newPassword) {
        if(isPasswordPatternNotValid(newPassword))
            return RequestFieldError.builder().field("newPassword").errorReason(ErrorReason.PATTERN).message("newPassword", ALLOW_PASSWORD_CHARS).build();
        return null;
    }

    public boolean isPasswordPatternNotValid(String password) {
        return !password.matches("^[\\w!@#$%^&*()\\-+=]+$");
    }

    public RequestFieldError checkRepeatedPasswordEqualing(String newPassword, String repeatedPassword) {
        if(isRepeatedPasswordNotEqual(newPassword, repeatedPassword))
            return RequestFieldError.builder().field("repeatedPassword").errorReason(ErrorReason.NOT_EQUAL).message("repeatedPassword", "newPassword").build();
        return null;
    }

    public boolean isRepeatedPasswordNotEqual(String password, String repeatedPassword) {
        return !password.equals(repeatedPassword);
    }

    public RequestFieldError checkPasswordMatching(String password, String passwordFieldName) throws NullPointerException {
        if(isPasswordNotMatching(password))
            return RequestFieldError.builder().field(passwordFieldName).errorReason(ErrorReason.NOT_MATCH).message(passwordFieldName).build();
        return null;
    }

    public boolean isPasswordNotMatching(String password) throws NullPointerException {
        if(accountId == null)
            throw new NullPointerException();
        boolean isMatch = false;
        Optional<Account> account = repository.findById(accountId);
        if(account.isPresent()) {
            isMatch = encoder.matches(password, account.get().getPassword());
        }
        return !isMatch;
    }
}