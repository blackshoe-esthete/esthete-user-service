package com.blackshoe.esthete.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserException extends RuntimeException {
    private final UserErrorResult userErrorResult;
    public UserException(UserErrorResult userErrorResult, String message) {
        super(message);
        this.userErrorResult = userErrorResult;
    }
}