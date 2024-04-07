package com.blackshoe.esthete.exception;

public enum TokenErrorResult {
    TOKEN_EXPIRED("Token expired"),
    INVALID_TOKEN("Invalid token"),
    // 다른 오류 결과들도 추가할 수 있습니다
    ;

    private final String message;

    TokenErrorResult(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
