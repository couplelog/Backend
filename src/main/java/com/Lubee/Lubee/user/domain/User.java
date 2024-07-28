package com.Lubee.Lubee.user.domain;

import com.Lubee.Lubee.common.BaseEntity;
import com.Lubee.Lubee.common.enumSet.LoginType;
import com.Lubee.Lubee.common.enumSet.UserRoleEnum;
import com.Lubee.Lubee.couple.domain.Couple;
import com.Lubee.Lubee.date_comment.domain.DateComment;
import com.Lubee.Lubee.enumset.Profile;
import com.Lubee.Lubee.firebase.domain.FireBase;
import com.Lubee.Lubee.user.dto.SignupDto;
import com.Lubee.Lubee.user_calendar_memory.domain.UserCalendarMemory;
import com.Lubee.Lubee.user_memory.domain.UserMemory;
import com.Lubee.Lubee.user_memory_reaction.domain.UserMemoryReaction;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@Table(name = "user")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_id", insertable=false, updatable=false)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = true, unique = true)
    private String email;

    @DateTimeFormat(pattern = "yyyy.MM.dd")
    @Temporal(TemporalType.DATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd", timezone = "Asia/Seoul")
    private Date birthday;

    private Profile profile;

    private String nickname;

    private boolean alreadyCouple = false;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private UserRoleEnum role;

    @OneToMany(mappedBy = "user")
    private List<DateComment> dateComments = new ArrayList<>();

    @OneToMany(mappedBy = "user",cascade = CascadeType.ALL)
    private List<UserMemory> userMemories = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<UserCalendarMemory> userCalendarMemories = new ArrayList<>();

    @ManyToOne(optional = true)
    @JoinColumn(name = "couple_id")
    private Couple couple;

    @OneToMany(mappedBy = "user")
    private List<UserMemoryReaction> userMemoryReactions = new ArrayList<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private FireBase fireBase;

    private LoginType loginType;

    private String kakaoRefreshToken;

    @Builder
    public User(LoginType socialType, String socialId) {
        this.loginType = socialType;
        this.username = socialId;
    }

    public User(String username, String email, UserRoleEnum role, String refreshToken, LoginType loginType) {
        this.username = username;
        this.email = email;
        this.role = role;
        this.kakaoRefreshToken = refreshToken;
        this.loginType = loginType;
    }

    public User(String username, String password, String email, Date birthday, Profile profile,  String nickname) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.birthday = birthday;
        this.profile = profile;
        this.nickname = nickname;
        this.role = UserRoleEnum.USER;
    }

    public static User of(LoginType loginType, String username, String password, UserRoleEnum role) {
        User user = new User(loginType, username);
        user.setPassword(password);
        user.setRole(role);
        return user;
    }

    public void linkCouple(Couple couple) {
        this.couple = couple;
        this.alreadyCouple = true;
    }

    public void UserSignup(User user, SignupDto signupDto)
    {

        user.setNickname(signupDto.getNickname());
        user.setProfile(signupDto.getProfile());
        user.setBirthday(signupDto.getBirthday());
    }

}
