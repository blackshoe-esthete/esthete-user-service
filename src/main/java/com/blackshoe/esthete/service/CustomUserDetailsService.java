package com.blackshoe.esthete.service;

import com.blackshoe.esthete.dto.CustomUserDetails;
import com.blackshoe.esthete.dto.LoginDto;
import com.blackshoe.esthete.entity.User;
import com.blackshoe.esthete.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("USerDetailsService(loadUser): "+username);
        //DB에서 조회
        User userData = userRepository.findByEmail(username);
//        LoginDto.IDPWDto idpwDto = LoginDto.IDPWDto.builder()
//                .email(userData.getEmail())
//                .password("tempPW")
//                .role(userData.getRole().toString())
//                .build();

        if(userData != null){
            //UserDetails에 담아서 return하면 AuthenticationManager가 검증함
            System.out.println("유저정보가 존재함");
            return  new CustomUserDetails(userData);
        }

        System.out.println("유저정보가 존재하지 않음");
        return null;
    }
}
