package com.Lubee.Lubee.couple.service;

import com.Lubee.Lubee.anniversary.dto.AnniversaryListDto;
import com.Lubee.Lubee.anniversary.service.AnniversaryService;
import com.Lubee.Lubee.common.api.ApiResponseDto;
import com.Lubee.Lubee.common.api.ErrorResponse;
import com.Lubee.Lubee.common.api.ResponseUtils;
import com.Lubee.Lubee.common.api.SuccessResponse;
import com.Lubee.Lubee.common.enumSet.ErrorType;
import com.Lubee.Lubee.common.exception.RestApiException;
import com.Lubee.Lubee.couple.domain.Couple;
import com.Lubee.Lubee.couple.dto.CoupleInfoDto;
import com.Lubee.Lubee.couple.dto.LinkCoupleRequest;
import com.Lubee.Lubee.couple.dto.LubeeCodeResponse;
import com.Lubee.Lubee.couple.repository.CoupleRepository;
import com.Lubee.Lubee.enumset.Profile;
import com.Lubee.Lubee.user.domain.User;
import com.Lubee.Lubee.user.repository.UserRepository;
import com.Lubee.Lubee.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CoupleService {

    private static final long LUBEE_CODE_EXPIRATION_MINUTES = 1440; // 24시간(분 단위)

    private final UserRepository userRepository;
    private final CoupleRepository coupleRepository;
    private final UserService userService;
    private final AnniversaryService anniversaryService;

    @Autowired
    private RedisTemplate<Long, String> redisTemplate;      // key-userid, value-lubeecode
    @Autowired
    private RedisTemplate<String, Long> reverseRedisTemplate;      // key-lubeecode, value-userid

    /**
     * <러비코드 생성/조회>
     *     - 10자리의 랜덤한 코드 생성
     *     - Userid가 key인 redisTemplate 생성
     *     - lubeecode가 key인 reverseRedisTemplate 생성
     */
    @Transactional
    public ApiResponseDto<LubeeCodeResponse> getLubeeCode(UserDetails userDetails) {

        final User user = userService.getUser(userDetails);

        if(user.isAlreadyCouple()){     // user가 이미 커플이면 러비코드 조회, 생성 불가능
            return ResponseUtils.ok(LubeeCodeResponse.of("ALREADY_COUPLE"), ErrorResponse.builder().status(200).message("이미 커플인 유저는 러비코드가 없습니다.").build());
        }

        String lubeeCode = redisTemplate.opsForValue().get(user.getId());
        if (lubeeCode == null) {        // 기존에 lubeecode 없으면 새로 생성
            lubeeCode = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
            // 저장
            redisTemplate.opsForValue().set(user.getId(), lubeeCode, Duration.ofMinutes(LUBEE_CODE_EXPIRATION_MINUTES));
            reverseRedisTemplate.opsForValue().set(lubeeCode, user.getId(),Duration.ofMinutes(LUBEE_CODE_EXPIRATION_MINUTES));
        }

        return ResponseUtils.ok(LubeeCodeResponse.of(lubeeCode), ErrorResponse.builder().status(200).message("요청 성공").build());
    }

    /**
     * <커플 연동>
     *     - requester, receiver가 이미 커플인지 확인한 후 커플 연동
     *     - 두 user가 커플 연결됐을 경우, DB에서 두 러비코드 삭제
     */
    @Transactional
    public ApiResponseDto<SuccessResponse> linkCouple(UserDetails userDetails, LinkCoupleRequest linkCoupleRequest) {

        User requester = userService.getUser(userDetails);
        if (requester.isAlreadyCouple()) {
            return ResponseUtils.ok(SuccessResponse.of(HttpStatus.NO_CONTENT, "requester가 이미 커플입니다."), ErrorResponse.builder().status(204).message("요청 성공").build());
        }

        Long receiverId = reverseRedisTemplate.opsForValue().get(linkCoupleRequest.getInputCode()); // 입력한 러비코드로 연인 찾기
        if(receiverId == null) {
            throw new RestApiException(ErrorType.LUBEE_CODE_NOT_FOUND);
        }

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RestApiException(ErrorType.NOT_FOUND_USER));

        if (receiver.isAlreadyCouple()) {
            return ResponseUtils.ok(SuccessResponse.of(HttpStatus.NO_CONTENT, "receiver가 이미 커플입니다."), ErrorResponse.builder().status(204).message("요청 성공").build());
        }

        Couple couple = Couple.builder()
                .receiver(receiver)
                .requester(requester)
                .build();
        coupleRepository.save(couple);

        requester.linkCouple(couple);
        receiver.linkCouple(couple);
        userRepository.save(requester);
        userRepository.save(receiver);

        String lubeeCode_requester = redisTemplate.opsForValue().get(requester.getId());    // requester의 러비코드는 필요 없어짐!
        if (lubeeCode_requester != null) {
            reverseRedisTemplate.delete(lubeeCode_requester);
        }
        reverseRedisTemplate.delete(linkCoupleRequest.getInputCode());      // 이미 사용된 러비코드는 지우기 - receiver의 러비코드

        redisTemplate.delete(receiver.getId());         // 커플된 유저의 러비코드는 삭제하기
        redisTemplate.delete(requester.getId());        // 만약 해당 key가 없어도 무시됨

        return ResponseUtils.ok(SuccessResponse.of(HttpStatus.OK, "커플 연결 완료"), ErrorResponse.builder().status(200).message("요청 성공").build());
    }

    /**
     * CoupleInfo 찾기
     */
    @Transactional(readOnly = true)
    public ApiResponseDto<CoupleInfoDto> getCoupleInfo(UserDetails loginUser)
    {
        User user = userRepository.findUserByUsername(loginUser.getUsername()).orElseThrow(
                () -> new RestApiException(ErrorType.NOT_FOUND_USER)
        );
        Couple couple = coupleRepository.findCoupleByUser(user).orElseThrow(
                () -> new RestApiException(ErrorType.NOT_FOUND_COUPLE)
        );
        User user_second = userRepository.findRestUser(user, couple).orElseThrow(
                () -> new RestApiException(ErrorType.NOT_FOUND_USER)
        );
        AnniversaryListDto anniversaryListDto = anniversaryService.getAnniversaryInfo(couple);
        System.out.println(user.getBirthday());
        CoupleInfoDto coupleInfoDto = CoupleInfoDto.of(
                user.getNickname(),
                user.getProfile(),
                user_second.getNickname(),
                user_second.getProfile(),
                user.getBirthday(),
                user_second.getBirthday(),
                anniversaryListDto

        );
        if (user.getNickname() == null && user_second.getNickname() == null) {
            return ResponseUtils.ok(coupleInfoDto, ErrorResponse.builder().status(200).message("커플 정보 없음").build());
        } else if (user.getNickname() == null) {
            return ResponseUtils.ok(coupleInfoDto, ErrorResponse.builder().status(200).message("내 정보 없음").build());
        } else if (user_second.getNickname() == null) {
            return ResponseUtils.ok(coupleInfoDto, ErrorResponse.builder().status(200).message("파트너 정보 없음").build());
        } else {
            return ResponseUtils.ok(coupleInfoDto, ErrorResponse.builder().status(200).message("요청 성공").build());
        }

    }

    /**
     *  User로 Couple 찾기
     */
    @Transactional(readOnly = true)
    public Couple getCoupleByUser(User user)
    {
        return coupleRepository.findCoupleByUser(user).
                orElseThrow(() ->new RestApiException(ErrorType.NOT_FOUND_COUPLE));
    }

    @Transactional(readOnly = true)
    public ApiResponseDto<SuccessResponse> breakCouple(UserDetails loginUser)
    {
        User user = userService.getUser(loginUser);
        Couple couple = getCoupleByUser(user);
        coupleRepository.delete(couple);
        return ResponseUtils.ok(SuccessResponse.of(HttpStatus.OK, "해당 Couple 연결이 끊어졌습니다."), ErrorResponse.builder().status(200).message("요청 성공").build());
    }

}
