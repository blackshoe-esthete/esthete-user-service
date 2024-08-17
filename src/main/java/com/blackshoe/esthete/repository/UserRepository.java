package com.blackshoe.esthete.repository;

import com.blackshoe.esthete.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    Optional<User> findByUuid(UUID uuid);

    Optional<User> findByEmail(String email);
//    User findByEmail(String email);

    Optional<User> findByEmailAndProvider(String email, String provider);

    Optional<User> findByKakaoIdAndProvider(String kakaoId, String provider);

    Optional<User> findByNaverIdAndProvider(String naverId, String provider);

}
