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
    OAuth2Dto.OAuth2CheckResponseDto socialLogin(OAuth2Dto.OAuth2CheckDto requestDto, String accessToken);
    OAuth2Dto.OAuth2ResponseDto socialLoginForSignUp(OAuth2Dto.OAuth2SignUpRequestDto requestDto, String accessToken);
    void checkUserId(LoginDto.FindIDRequestDto requestDto);
    LoginDto.FindPasswordResponseDto resetPassword(LoginDto.FindPasswordRequestDto requestDto);


}
