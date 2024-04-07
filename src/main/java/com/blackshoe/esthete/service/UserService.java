package com.blackshoe.esthete.service;

import com.blackshoe.esthete.dto.SignUpDto;
import com.blackshoe.esthete.entity.Gender;
import com.blackshoe.esthete.entity.Role;
import com.blackshoe.esthete.entity.User;
import com.blackshoe.esthete.exception.UserErrorResult;
import com.blackshoe.esthete.exception.UserException;
import com.blackshoe.esthete.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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

    public SignUpDto.ESTSignUpCompletionResponseDto joinUserCompletion(SignUpDto.ESTSignUpCompletionRequestDto requestDto) {
        User user = userRepository.findByUuid(requestDto.getUserId()).orElseThrow(() -> new UserException(UserErrorResult.NOT_FOUND_USER)); // 나중에 UserId로 바꾸기

        String nickname = requestDto.getNickname();
        Gender gender = requestDto.getGender();
        LocalDate birthday = requestDto.getBirthday();

        user.addUserInfo(nickname, gender,birthday);

        User savedUser = userRepository.save(user);

        return SignUpDto.ESTSignUpCompletionResponseDto.builder()
                .createdAt(savedUser.getCreatedAt())
                .build();
    }

    public boolean userExistByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean isValidDate(LocalDate birthday) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String formattedDate = birthday.format(formatter);
            LocalDate parsedDate = LocalDate.parse(formattedDate, formatter);
            return parsedDate.equals(birthday);
        } catch (DateTimeParseException e) {
            return false;
        }
    }

}
