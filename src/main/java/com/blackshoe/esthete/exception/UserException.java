package com.blackshoe.esthete.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public class UserException extends RuntimeException {
    private final UserErrorResult userErrorResult;
    public UserException(UserErrorResult userErrorResult) {
        super(userErrorResult.getMessage());
        this.userErrorResult = userErrorResult;
    }
}