package com.blackshoe.esthete.service;

import com.blackshoe.esthete.dto.LoginDto;
import com.blackshoe.esthete.dto.OAuth2Dto;
import com.blackshoe.esthete.dto.SignUpDto;

import java.time.LocalDate;

public interface UserService {
    SignUpDto.ESTSignUpNextResponseDto joinUserNext(SignUpDto.ESTSignUpNextRequestDto requestDto);
    SignUpDto.ESTSignUpCompletionResponseDto joinUserCompletion(SignUpDto.ESTSignUpCompletionRequestDto requestDto);
    boolean userExistByEmail(String email);
    boolean isValidDate(LocalDate birthday);
    OAuth2Dto.OAuth2ResponseDto socialLogin(OAuth2Dto.OAuth2RequestDto requestDto);
    void checkUserId(LoginDto.FindIDRequestDto requestDto);
    LoginDto.FindPasswordResponseDto resetPassword(LoginDto.FindPasswordRequestDto requestDto);


}
