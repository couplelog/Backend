package com.Lubee.Lubee.date_comment.service;

import com.Lubee.Lubee.calendar.domain.Calendar;
import com.Lubee.Lubee.calendar.repository.CalendarRepository;
import com.Lubee.Lubee.common.api.ApiResponseDto;
import com.Lubee.Lubee.common.api.ErrorResponse;
import com.Lubee.Lubee.common.api.ResponseUtils;
import com.Lubee.Lubee.common.api.SuccessResponse;
import com.Lubee.Lubee.common.enumSet.ErrorType;
import com.Lubee.Lubee.common.exception.RestApiException;
import com.Lubee.Lubee.couple.domain.Couple;
import com.Lubee.Lubee.couple.repository.CoupleRepository;
import com.Lubee.Lubee.date_comment.domain.DateComment;
import com.Lubee.Lubee.date_comment.dto.*;
import com.Lubee.Lubee.date_comment.repository.DateCommentRepository;
import com.Lubee.Lubee.user.domain.User;
import com.Lubee.Lubee.user.repository.UserRepository;
import com.Lubee.Lubee.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DateCommentService {

    private final UserRepository userRepository;
    private final DateCommentRepository dateCommentRepository;
    private final CoupleRepository coupleRepository;
    private final CalendarRepository calendarRepository;
    private final UserService userService;

    /**
     * <데이트 코멘트 생성>
     *     - 아직 캘린더가 없으면, 해당 날짜의 캘린더를 생성
     *     - 데이트코멘트가 생성되면, Calendar에도 추가
     */
    @Transactional
    public ApiResponseDto<SuccessResponse> createDateComment(UserDetails userDetails, CreateDateCommentRequest createDateCommentRequest) {

        final User user = userService.getUser(userDetails);
        final Couple couple = getUserCouple(user);

        // 캘린더가 없으면 해당 날짜의 캘린더를 생성
        Calendar calendar = calendarRepository.findByCoupleAndEventDate(couple, createDateCommentRequest.getDate());
        if (calendar == null) {
            log.info("캘린더가 존재하지 않아, 새롭게 캘린더를 생성했습니다.");
            calendar = Calendar.builder()
                    .couple(couple)
                    .eventDate(createDateCommentRequest.getDate())
                    .build();
            calendarRepository.save(calendar);
        }
        else{
            // 해당 날짜에 이미 작성한 데이트코멘트가 있으면...
            if(dateCommentRepository.findByUserAndCoupleAndCalendar_EventDate(user, couple, createDateCommentRequest.getDate()).isPresent()) {
                throw new RestApiException(ErrorType.DATE_COMMENT_ALREADY_EXIST);
            }
        }

        DateComment dateComment = DateComment.builder()
                .user(user)
                .couple(couple)
                .content(createDateCommentRequest.getContent())
                .calendar(calendar)
                .build();
        calendar.addDateComment(dateComment);    // 양방향 일대다 관계이기 때문
        dateCommentRepository.save(dateComment);

        return ResponseUtils.ok(SuccessResponse.of(HttpStatus.OK, "데이트코멘트 생성 완료"), ErrorResponse.builder().status(200).message("요청 성공").build());
    }

    /**
     * <커플의 데이트코멘트 조회 (날짜 하루)>
     *     - 파라미터에 따라 User, Couple, Calendar 조회 -> 에러 반환
     *     - (1) 둘다 작성 x => 에러 반환
     *     - (2) 나만 작성 o => 나의 데이트코멘트만 반환
     *     - (3) 둘다 작성 o => 둘의 데이트코멘트 모두 반환
     */
    @Transactional(readOnly = true)
    public ApiResponseDto<TodayDateCommentResponse> findTodayDateCommentByCouple(UserDetails userDetails, Date date) {

        final User user = userService.getUser(userDetails);
        final Couple couple = getUserCouple(user);


        final Calendar calendar = calendarRepository.findByCoupleAndEventDate(couple, date);
        if (calendar == null) {               // 커플이 해당 달력을 만들지 않았으면..
            return ResponseUtils.ok(
                    null,
                    ErrorResponse.builder().status(200).message("커플이 해당 날짜에 기록한 내용이 없습니다.").build()
            );
        }

        TodayDateCommentResponse todayDateCommentResponses = new TodayDateCommentResponse();
        User lover = findOtherUserInCouple(user.getId(), couple);

        // 커플 모두가 작성한 데이트 코멘트 수 확인
        List<DateComment> dateComments = dateCommentRepository.findByCoupleAndCalendar(couple, calendar);
        DateComment myComment = dateCommentRepository.findByUserAndCalendar(user, calendar);

        if (dateComments.isEmpty()) {     // 둘다 코멘트 작성x
            return ResponseUtils.ok(
                    null,
                    ErrorResponse.builder().status(200).message("커플 모두 데이트 코멘트를 작성하지 않았습니다.").build()
            );
        }

        if (dateComments.size() == 1 && myComment != null) {    // 나만 작성
            todayDateCommentResponses.setMine(DateCommentBaseDto.from(myComment, user));
            todayDateCommentResponses.setLover(DateCommentBaseDto.from(null, lover));
            return ResponseUtils.ok(todayDateCommentResponses, ErrorResponse.builder().status(200).message("상대방은 데이트코멘트를 작성하지 않았습니다.").build());
        }

        // 둘 다 작성한 경우 => 상대방의 것도 열람 가능
        DateComment otherUserComment = dateCommentRepository.findByUserAndCalendar(lover, calendar);

        todayDateCommentResponses.setMine(DateCommentBaseDto.from(myComment, user));
        todayDateCommentResponses.setLover(DateCommentBaseDto.from(otherUserComment, lover));

        return ResponseUtils.ok(
                todayDateCommentResponses,
                ErrorResponse.builder().status(200).message("두 사람 모두 데이트코멘트를 작성했습니다.").build()
        );
    }

    /**
     * <데이트코멘트 수정>
     *     - 파라미터로 User, DateComment 조회 => 에러 반환
     *     - 작성자와 현재 사용자의 id 비교 -- 권한o 유저만 내용 변경O
     */
    @Transactional
    public SuccessResponse changeContent(UserDetails userDetails, UpdateDateCommentRequest updateDateCommentRequest) {

        final User user = userService.getUser(userDetails);
        final Couple couple = getUserCouple(user);

        Optional<DateComment> dateComment = dateCommentRepository.findByUserAndCoupleAndCalendar_EventDate(user, couple, updateDateCommentRequest.getDate());
        if(dateComment.isEmpty()) {
            throw new RestApiException(ErrorType.NOT_FOUND_DATE_COMMENT);
        }
        else{
            dateComment.get().changeContent(updateDateCommentRequest.getContent());
            dateCommentRepository.save(dateComment.get());

            return SuccessResponse.builder().status(200).message("데이트 코멘트 수정 성공").build();
        }

    }

    /**
     * Couple과 User 1명만 알 때 - 연인(User) 찾기
     */
    public User findOtherUserInCouple(Long knownUserId, Couple couple) {

        if (couple != null && couple.getUser().size() == 2) {
            // Couple에는 항상 2명의 사용자가 포함되므로, 알고 있는 사용자를 제외한 다른 사용자를 찾습니다.
            for (User user : couple.getUser()) {
                if (!user.getId().equals(knownUserId)) {
                    return user;
                }
            }
        }
        return null; // 적절한 Couple을 찾지 못한 경우 null 반환
    }

    public List<DateCommentBaseDto> getDateCommentsByCoupleAndCalendar(Couple couple, Date today) {
        List<DateComment> dateComments = dateCommentRepository.findByCoupleAndCalendarEventDate(couple, today);

        return dateComments.stream()
                .map(dateComment -> DateCommentBaseDto.of(
                        dateComment.getContent(),
                        dateComment.getUser().getProfile()))
                .collect(Collectors.toList());
    }

    // 유저로 커플 찾는 함수
    private Couple getUserCouple(User user) {
        // Find the Couple associated with the User
        Optional<Couple> optionalCouple = coupleRepository.findCoupleByUser(user);

        if (optionalCouple.isEmpty()) {
            throw new RestApiException(ErrorType.NOT_FOUND_COUPLE);
        }

        return optionalCouple.get();
    }
}