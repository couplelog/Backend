package com.Lubee.Lubee.user.service;

import com.Lubee.Lubee.common.api.ApiResponseDto;
import com.Lubee.Lubee.common.api.ErrorResponse;
import com.Lubee.Lubee.common.api.ResponseUtils;
import com.Lubee.Lubee.common.api.SuccessResponse;
import com.Lubee.Lubee.common.enumSet.ErrorType;
import com.Lubee.Lubee.common.exception.RestApiException;
import com.Lubee.Lubee.couple.domain.Couple;
import com.Lubee.Lubee.couple.repository.CoupleRepository;
import com.Lubee.Lubee.user.domain.User;
import com.Lubee.Lubee.user.dto.SignupDto;
import com.Lubee.Lubee.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CoupleRepository coupleRepository;
    private final JdbcTemplate jdbcTemplate;
    // 추후 수정
    @Transactional(readOnly = true)
    public User getUser(UserDetails loginUser)
    {

        return userRepository.findByUsername(loginUser.getUsername())
                .orElseThrow(() -> new RestApiException(ErrorType.NOT_FOUND_USER));
    }

    @Transactional
    public ApiResponseDto<SuccessResponse> onBoarding(UserDetails loginUser, SignupDto signupDto)
    {
        //System.out.println(loginUser);
        User user = userRepository.findUserByUsername(loginUser.getUsername()).orElseThrow(
                () ->  new RestApiException(ErrorType.NOT_FOUND_USER)
        );
        user.setProfile(signupDto.getProfile());
        user.setNickname(signupDto.getNickname());
        user.setBirthday(signupDto.getBirthday());
        Couple couple = coupleRepository.findCoupleByUser(user).orElseThrow(
                () -> new RestApiException(ErrorType.NOT_FOUND_COUPLE)
        );
        if (couple == null)
        {
            throw new RestApiException(ErrorType.NOT_COUPLE);
        }
        couple.setStartDate(signupDto.getStartDate());
        userRepository.save(user);
        coupleRepository.save(couple);

        return ResponseUtils.ok(SuccessResponse.of(HttpStatus.OK, "온보딩 완료"), ErrorResponse.builder().status(200).message("요청 성공").build());
    }



    @Transactional
    public ApiResponseDto<SuccessResponse> resetDataBase()
    {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0;");
        jdbcTemplate.execute("DELETE FROM anniversary;");
        jdbcTemplate.execute("DELETE FROM calendar;");

        jdbcTemplate.execute("DELETE FROM calendar_memory;");
        jdbcTemplate.execute("DELETE FROM couple;");

        jdbcTemplate.execute("DELETE FROM date_comment;");
        jdbcTemplate.execute("DELETE FROM fire_base;");

        jdbcTemplate.execute("DELETE FROM memory;");

        jdbcTemplate.execute("DELETE FROM user;");
        jdbcTemplate.execute("DELETE FROM user_calendar_memory;");

        jdbcTemplate.execute("DELETE FROM user_memory;");
        jdbcTemplate.execute("DELETE FROM user_memory_reaction;");
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1;");

        return ResponseUtils.ok(SuccessResponse.of(HttpStatus.OK, "DB 리셋 완료"), ErrorResponse.builder().status(200).message("요청 성공").build());
    }

}
