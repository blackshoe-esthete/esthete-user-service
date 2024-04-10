package com.blackshoe.esthete.service;

import com.blackshoe.esthete.dto.CustomUserDetails;
import com.blackshoe.esthete.dto.LoginDto;
import com.blackshoe.esthete.entity.User;
import com.blackshoe.esthete.exception.UserErrorResult;
import com.blackshoe.esthete.exception.UserException;
import com.blackshoe.esthete.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("UserDetailsService(loadUser): "+username);
        //DB에서 조회
        User userData = userRepository.findByEmail(username).orElseThrow(() -> new UserException(UserErrorResult.NOT_FOUND_USER));
        System.out.println("유저정보가 존재함");
        return  new CustomUserDetails(userData);
    }

//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        System.out.println("UserDetailsService(loadUser): "+username);
//        Optional<User> userOptional = userRepository.findByEmail(username);
//        if (userOptional.isPresent()) {
//            User user = userOptional.get();
//            return new CustomUserDetails(user);
//        } else {
//            // 사용자를 찾지 못한 경우, 빈 Optional이나 null을 반환하도록 수정
//            return null; // 또는 Optional.empty();
//        }
//    }

//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        System.out.println("USerDetailsService(loadUser): "+username);
//        //DB에서 조회
//        User userData = userRepository.findByEmail(username);
//
//
//        if(userData != null){
//            //UserDetails에 담아서 return하면 AuthenticationManager가 검증함
//            System.out.println("유저정보가 존재함");
//            return  new CustomUserDetails(userData);
//        }
//
//        System.out.println("유저정보가 존재하지 않음");
//        return null;
//    }
}
