package com.blackshoe.esthete.exception;

public enum TokenErrorResult {
    TOKEN_EXPIRED("Token expired"),
    INVALID_TOKEN("Invalid token"),
    ;

    private final String message;

    TokenErrorResult(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
