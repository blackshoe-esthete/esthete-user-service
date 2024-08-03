package com.blackshoe.esthete.service;

import com.blackshoe.esthete.dto.KafkaProducerDto;
import com.blackshoe.esthete.dto.LoginDto;
import com.blackshoe.esthete.dto.OAuth2Dto;
import com.blackshoe.esthete.dto.SignUpDto;
import com.blackshoe.esthete.entity.Gender;
import com.blackshoe.esthete.entity.Role;
import com.blackshoe.esthete.entity.User;
import com.blackshoe.esthete.exception.UserErrorResult;
import com.blackshoe.esthete.exception.UserException;
import com.blackshoe.esthete.repository.UserRepository;
import com.blackshoe.esthete.service.kafka.KafkaUserInfoProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final KafkaUserInfoProducerService kafkaUserInfoProducerService;

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

        // kafka로 새로운 회원정보 넘겨주기
        KafkaProducerDto.UserCreate userCreate = KafkaProducerDto.UserCreate.builder()
                .userId(savedUser.getUuid())
                .nickname(savedUser.getNickname())
                .email(savedUser.getEmail())
                .gender(savedUser.getGender())
                .birthday(savedUser.getBirthday())
                .build();

        kafkaUserInfoProducerService.createUser(userCreate);

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


    public OAuth2Dto.OAuth2CheckResponseDto socialLogin(OAuth2Dto.OAuth2CheckDto requestDto){
        String originalNickname = requestDto.getOriginalNickname();
        String provider = requestDto.getProvider();

        Optional<User> user = userRepository.findByOriginalNicknameAndProvider(originalNickname, provider);
        log.info("user info: " + user.get().getEmail());

        if(user.isPresent()){
            log.info("기존에 존재하는 회원입니다.");
            return OAuth2Dto.OAuth2CheckResponseDto.builder()
                    .isMembered(true)
                    .build();
        }
        else{
            log.info("존재하지 않는 회원입니다.");
            return OAuth2Dto.OAuth2CheckResponseDto.builder()
                    .isMembered(false)
                    .build();
        }
    }

    public OAuth2Dto.OAuth2ResponseDto socialLoginForSignUp(OAuth2Dto.OAuth2SignUpRequestDto requestDto){
        log.info("처음 로그인한 회원임으로 회원가입을 진행합니다. ");

        User newUser = User.builder()
                .uuid(UUID.randomUUID())
                .provider(requestDto.getProvider())
                .nickname(requestDto.getNickname())
                .originalNickname(requestDto.getOriginalNickname())
                .email(requestDto.getEmail())
                .role(Role.USER)
                .gender(requestDto.getGender())
                .birthday(requestDto.getBirthday())
                .build();

        User socialUser = userRepository.save(newUser);

        return OAuth2Dto.OAuth2ResponseDto.builder()
                .userId(socialUser.getUuid())
                .provider(socialUser.getProvider())
                .createdAt(socialUser.getCreatedAt())
                .build();
    }



    public void checkUserId(LoginDto.FindIDRequestDto requestDto) {
        if(!userRepository.existsByEmail(requestDto.getEmail())){
            throw new UserException(UserErrorResult.NOT_FOUND_USER);
        }
    }

    public LoginDto.FindPasswordResponseDto resetPassword(LoginDto.FindPasswordRequestDto requestDto) {
        User user = userRepository.findByEmail(requestDto.getEmail()).orElseThrow(() -> new UserException(UserErrorResult.NOT_FOUND_USER));

        user.updatePassword(bCryptPasswordEncoder.encode(requestDto.getNewPassword()));

        userRepository.save(user);

        return LoginDto.FindPasswordResponseDto.builder()
                .updatedAt(LocalDateTime.now().toString())
                .build();
    }
}
