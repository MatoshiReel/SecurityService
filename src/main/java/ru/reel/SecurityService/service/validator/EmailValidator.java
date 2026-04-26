package ru.reel.SecurityService.service.validator;

import org.springframework.stereotype.Component;
import ru.reel.SecurityService.dto.NewEmailDto;
import ru.reel.SecurityService.service.AccountService;
import ru.reel.request.error.RequestFieldError;
import ru.reel.request.error.reason.ErrorReason;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
public class EmailValidator implements Validator<RequestFieldError, List<RequestFieldError>, NewEmailDto> {
    private static final String EMAIL_PATTERN = "[local-part(A-Z a-z 0-9 . _ % + -)]@[domain(A-Z a-z 0-9 . -).(A-Z a-z)]";
    private final PasswordValidator passwordValidator;
    private final AccountService service;

    public EmailValidator(PasswordValidator passwordValidator, AccountService service) {
        this.passwordValidator = passwordValidator;
        this.service = service;
    }

    public void setAccountId(UUID accountId) {
        this.passwordValidator.setAccountId(accountId);
    }

    @Override
    public List<RequestFieldError> validate(NewEmailDto dto) {
        List<RequestFieldError> errors = new ArrayList<>();
        errors.add(passwordValidator.checkPasswordNullable(dto.password, "password"));
        errors.add(checkEmailNullable(dto.newEmail, "newEmail"));
        errors = new ArrayList<>(errors.stream().filter(Objects::nonNull).toList());
        if(!errors.isEmpty())
            return errors;
        errors.add(passwordValidator.checkPasswordMatching(dto.password, "password"));
        errors.add(checkNewEmailPatternMatching(dto.newEmail));
        errors.add(checkEmailExisting(dto.newEmail, "newEmail"));
        return errors.stream().filter(Objects::nonNull).toList();
    }

    public RequestFieldError checkEmailNullable(String email, String emailFieldName) {
        if(isEmpty(email))
            return RequestFieldError.builder()
                    .field(emailFieldName)
                    .errorReason(ErrorReason.EMPTY)
                    .message(emailFieldName)
                    .build();
        return null;
    }

    public boolean isEmpty(String text) {
        return text == null || text.isEmpty();
    }

    public RequestFieldError checkNewEmailPatternMatching(String newEmail) {
        if(isEmailPatternNotValid(newEmail))
            return RequestFieldError.builder()
                    .field("newEmail")
                    .errorReason(ErrorReason.PATTERN)
                    .message("newEmail", EMAIL_PATTERN)
                    .build();
        return null;
    }

    public boolean isEmailPatternNotValid(String email) {
        return !email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    public RequestFieldError checkEmailExisting(String email, String emailFieldName) {
        if(isEmailExisting(email))
            return RequestFieldError.builder()
                    .field(emailFieldName)
                    .errorReason(ErrorReason.EXIST)
                    .message("email")
                    .build();
        return null;
    }

    public boolean isEmailExisting(String email) {
        try {
            return service.isExistsByEmail(email);
        } catch (NullPointerException e) {
            return false;
        }
    }
}
