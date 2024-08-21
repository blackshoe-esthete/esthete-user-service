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
import org.json.JSONObject;
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
    private final Oauth2Service oauth2Service;

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


    public OAuth2Dto.OAuth2CheckResponseDto socialLogin(OAuth2Dto.OAuth2CheckDto requestDto, String accessToken){
        if(requestDto.getProvider().equals("kakao")){
            String kakaoId = requestDto.getKakaoId();
            String provider = requestDto.getProvider();

            Optional<User> kakaoUser = userRepository.findByKakaoIdAndProvider(kakaoId, provider);
            log.info("kakao repository에서 찾은 후");

            if(kakaoUser.isPresent()){
                String checkedKakaoId = oauth2Service.getUserInfo(provider, accessToken);
                if(kakaoId.equals(checkedKakaoId)){
                    log.info("기존에 존재하는 카카오 회원입니다.");
                    return OAuth2Dto.OAuth2CheckResponseDto.builder()
                            .isMembered(true)
                            .build();
                }else {
                    throw new IllegalArgumentException("카카오 ID가 일치하지 않습니다.");
                }
            }
            else{
                log.info("존재하지 않는 카카오 회원입니다.");
                return OAuth2Dto.OAuth2CheckResponseDto.builder()
                        .isMembered(false)
                        .build();
            }
        }
        else if(requestDto.getProvider().equals("google")){
            String email = requestDto.getEmail();
            String provider = requestDto.getProvider();

            Optional<User> user = userRepository.findByEmailAndProvider(email, provider);
            log.info("google repository에서 찾은 후");

            if(user.isPresent()){
                String checkedGoogleEmail = oauth2Service.getUserInfo(provider, accessToken);
                if(email.equals(checkedGoogleEmail)){
                    log.info("기존에 존재하는 구글 회원입니다.");
                    return OAuth2Dto.OAuth2CheckResponseDto.builder()
                            .isMembered(true)
                            .build();
                }
                else {
                    throw new IllegalArgumentException("구글 이메일이 일치하지 않습니다.");
                }
            }
            else{
                log.info("존재하지 않는 구글 회원입니다.");
                return OAuth2Dto.OAuth2CheckResponseDto.builder()
                        .isMembered(false)
                        .build();
            }
        }
        else if(requestDto.getProvider().equals("naver")){
            String naverId = requestDto.getNaverId();
            String provider = requestDto.getProvider();

            Optional<User> user = userRepository.findByNaverIdAndProvider(naverId, provider);
            log.info("naver repository에서 찾은 후");
            log.info("checkedEmail: " + naverId);

            if(user.isPresent()){
                String checkedNaverEmail = oauth2Service.getUserInfo(provider, accessToken);
                if(naverId.equals(checkedNaverEmail)){
                    log.info("기존에 존재하는 네이버 회원입니다.");
                    return OAuth2Dto.OAuth2CheckResponseDto.builder()
                            .isMembered(true)
                            .build();
                }
                else {
                    throw new IllegalArgumentException("네이버 이메일이 일치하지 않습니다.");
                }
            }
            else{
                log.info("존재하지 않는 네이버 회원입니다.");
                return OAuth2Dto.OAuth2CheckResponseDto.builder()
                        .isMembered(false)
                        .build();
            }
        }
        return OAuth2Dto.OAuth2CheckResponseDto.builder()
                .isMembered(false)
                .build();
    }

    public OAuth2Dto.OAuth2ResponseDto socialLoginForSignUp(OAuth2Dto.OAuth2SignUpRequestDto requestDto, String accessToken){
        log.info("처음 로그인한 회원임으로 회원가입을 진행합니다. ");
        String emailforcheck = requestDto.getEmail();
        if(userRepository.existsByEmail(emailforcheck)){
            throw new UserException(UserErrorResult.DUPLICATED_EMAIL);
        }

        if(requestDto.getProvider().equals("kakao")){
            String kakaoId = requestDto.getKakaoId();
            String provider = requestDto.getProvider();

            String checkedKakaoId = oauth2Service.getUserInfo(provider, accessToken);
            if(kakaoId.equals(checkedKakaoId)) {
                log.info("유효한 kakao 토큰입니다.");

                User newUser = User.builder()
                        .uuid(UUID.randomUUID())
                        .provider(requestDto.getProvider())
                        .nickname(requestDto.getNickname())
                        .kakaoId(checkedKakaoId)
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
            }else {
                throw new IllegalArgumentException("유효하지 않은 kakao 토큰입니다.");
            }

        }
        else if(requestDto.getProvider().equals("google")){
            String email = requestDto.getEmail();
            String provider = requestDto.getProvider();

            String checkedNaverId = oauth2Service.getUserInfo(provider, accessToken);
            if(email.equals(checkedNaverId)) {
                log.info("유효한 google 토큰입니다.");

                User newUser = User.builder()
                        .uuid(UUID.randomUUID())
                        .provider(requestDto.getProvider())
                        .nickname(requestDto.getNickname())
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
            }else {
                throw new IllegalArgumentException("유효하지 않은 google 토큰입니다.");
            }
        }
        else if(requestDto.getProvider().equals("naver")){
            String naverId = requestDto.getNaverId();
            log.info("naverId : " + naverId);
            String provider = requestDto.getProvider();

            String checkedEmail = oauth2Service.getUserInfo(provider, accessToken);
            log.info("checkedEmail : " + checkedEmail);

            if(naverId.equals(checkedEmail)) {
                log.info("유효한 naver 토큰입니다.");

                User newUser = User.builder()
                        .uuid(UUID.randomUUID())
                        .provider(requestDto.getProvider())
                        .nickname(requestDto.getNickname())
                        .naverId(checkedEmail)
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
            }else {
                throw new IllegalArgumentException("유효하지 않은 naver 토큰입니다.");
            }
        }

        return OAuth2Dto.OAuth2ResponseDto.builder()
                .userId(null)
                .provider(null)
                .createdAt(null)
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
