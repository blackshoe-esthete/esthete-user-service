package com.blackshoe.esthete.service;

import com.blackshoe.esthete.dto.SignUpDto;
import com.blackshoe.esthete.entity.Role;
import com.blackshoe.esthete.entity.User;
import com.blackshoe.esthete.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public SignUpDto.ESTSignUpNextResponseDto joinUserNext(SignUpDto.ESTSignUpNextRequestDto requestDto) {// 얘는 그냥 회원가입 폼
        User newUser = User.builder()
                .uuid(UUID.randomUUID())
                .provider("Esthete")
                .email(requestDto.getEmail())
                .password(bCryptPasswordEncoder.encode(requestDto.getPassword()))
                .role(Role.USER)
                .build();

        User savedUser = userRepository.save(newUser);

        return SignUpDto.ESTSignUpNextResponseDto.builder()
                .userId(savedUser.getUuid())
                .createdAt(savedUser.getCreatedAt())
                .build();
    }

    public boolean userExistByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

}
