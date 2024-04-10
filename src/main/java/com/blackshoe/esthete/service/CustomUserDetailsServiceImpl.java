package com.blackshoe.esthete.service;

import com.blackshoe.esthete.dto.CustomUserDetails;
import com.blackshoe.esthete.dto.LoginDto;
import com.blackshoe.esthete.entity.User;
import com.blackshoe.esthete.exception.UserErrorResult;
import com.blackshoe.esthete.exception.UserException;
import com.blackshoe.esthete.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsServiceImpl implements UserDetailsService, CustomUserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        System.out.println("UserDetailsService(loadUser): "+username);
        //DB에서 조회
        User userData = userRepository.findByEmail(username).orElseThrow(() -> new UserException(UserErrorResult.NOT_FOUND_USER));
        System.out.println("유저정보가 존재함");
        return  new CustomUserDetails(userData);
    }

}
