package com.blackshoe.esthete.service;

import com.blackshoe.esthete.dto.OAuth2Dto;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;

public interface SecurityService {
    Map<String, String> saveUserInSecurityContext(OAuth2Dto.OAuth2RequestDto requestDto);
    Map<String, String> saveUserInSecurityContext(String socialId, String socialProvider);
    UserDetails loadUserBySocialIdAndSocialProvider(String socialId, String socialProvider);


}
