package ru.reel.SecurityService.service.validator;

import ru.reel.request.error.RequestError;

import java.util.List;

/**
 * @param <T> validated object.
 * @param <R> returned error type {@link RequestError}, not null if an error was detected during validation process.
 * @see RequestError
 */
public interface Validator<R extends RequestError, L extends List<R>, T> {
    L validate(T obj);
}