package com.blackshoe.esthete.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorResult {
    REQUIRED_VALUE(HttpStatus.BAD_REQUEST, "필수 값이 누락되었습니다."), //400
    INVALID_EMAIL(HttpStatus.UNPROCESSABLE_ENTITY, "유효하지 않은 이메일입니다."), //422
    INVALID_PASSWORD(HttpStatus.UNPROCESSABLE_ENTITY, "유효하지 않은 비밀번호(8자리 이상 20자리 이하이며 특수 문자($,@,!,%,*,#,?,&) 최소 하나 포함)입니다."),
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
    INVALID_NICKNAME(HttpStatus.UNPROCESSABLE_ENTITY, "유효하지 않은 닉네임(10자리 이상이거나, 특수문자 포함)입니다."),
    DUPLICATED_NICKNAME(HttpStatus.CONFLICT, "이미 존재하는 닉네임입니다."), //409
    DUPLICATED_EMAIL(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."), //409
    INVALID_BIRTHDAY(HttpStatus.UNPROCESSABLE_ENTITY, "유효하지 않은 생년월일(yyyy-MM-dd)입니다."),
    INVALID_PROVIDER(HttpStatus.UNPROCESSABLE_ENTITY, "유효하지 않은 Social Provider입니다."),
    INVALID_GENDER(HttpStatus.UNPROCESSABLE_ENTITY, "유효하지 않은 성별(MALE, FEMALE)입니다."),
    NOT_FOUND_JWT(HttpStatus.UNAUTHORIZED, "존재하지 않는 JWT입니다."), //401
    EXPIRED_JWT(HttpStatus.UNAUTHORIZED, "만료된 JWT입니다."),
    INVALID_JWT(HttpStatus.UNAUTHORIZED, "유효하지 않은 JWT입니다."),
    INVALID_AUTH_NUM(HttpStatus.UNPROCESSABLE_ENTITY, "유효하지 않은 인증번호입니다.");


    private final HttpStatus httpStatus;
    private final String message;
}
