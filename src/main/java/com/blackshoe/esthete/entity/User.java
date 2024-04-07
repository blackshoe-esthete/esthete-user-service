package com.blackshoe.esthete.entity;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.*;
import org.hibernate.type.SqlTypes;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "uuid")
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID uuid;

    @Column(name = "provider", length = 20)
    private String provider;

    @Column(name = "nickname", length = 100)
    private String nickname;

    @Column(name = "email", length = 50)
    private String email;

    @Column(name = "password", length = 100)
    private String password; // 소셜로그인은 해당x, 일반 회원에게 해당

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "birthday")
    private LocalDate birthday;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, length = 20)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", length = 20)
    private LocalDateTime updatedAt;



    public void addUserInfo(String nickname, Gender gender, LocalDate birthday) {
        this.nickname = nickname;
        this.gender = gender;
        this.birthday = birthday;
    }

    @Builder // 소셜로그인
    public User(UUID uuid, String provider, String nickname, String email, String password, Role role, Gender gender, LocalDate birthday, String phone) {
        this.uuid = uuid;
        this.provider = provider;
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        //this.role = Role.valueOf("USER");
        this.role = role;
        this.gender = gender;
        this.birthday = birthday;
    }

    public void updateUserInfo(String nickname, Gender gender, LocalDate birthday) {
        this.nickname = nickname;
        this.gender = gender;
        this.birthday = birthday;
    }



}
