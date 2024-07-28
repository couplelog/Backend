package com.Lubee.Lubee.calendar.service;

import com.Lubee.Lubee.calendar.domain.Calendar;
import com.Lubee.Lubee.calendar.repository.CalendarRepository;
import com.Lubee.Lubee.common.api.ApiResponseDto;
import com.Lubee.Lubee.common.api.ErrorResponse;
import com.Lubee.Lubee.common.api.ResponseUtils;
import com.Lubee.Lubee.couple.domain.Couple;
import com.Lubee.Lubee.couple.repository.CoupleRepository;
import com.Lubee.Lubee.couple.service.CoupleService;
import com.Lubee.Lubee.user.domain.User;
import com.Lubee.Lubee.user.repository.UserRepository;
import com.Lubee.Lubee.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class HoneyService {

    private final UserRepository userRepository;
    private final CoupleRepository coupleRepository;
    private final CalendarRepository calendarRepository;
    private final UserService userService;
    private final CoupleService coupleService;

    /**
     * <오늘의 꿀 개수 조회 (날짜 하루)>
     *     - 파라미터에 따라 User, Couple, Calendar 조회 -> 에러 반환
     *     - (1) 올라온 사진 X => 0 반환
     *     - (2) 올라온 사진 O => 사진 개수 반환
     */
    @Transactional(readOnly = true)
    public ApiResponseDto<Integer> getHoneyInfoByUserAndDate(UserDetails userDetails, Date date){

        final User user = userService.getUser(userDetails);
        final Couple couple = coupleService.getCoupleByUser(user);
        final Calendar calendar = calendarRepository.findByCoupleAndEventDate(couple, date);

        // 오늘의 허니 계산
        int todayHoney;
        if(calendar == null){
            todayHoney = 0;
        }
        else{
            todayHoney = calendar.getCalendarMemories().size();
        }

        return ResponseUtils.ok(
                todayHoney,
                ErrorResponse.builder().status(200).message("요청 성공").build()
        );
    }

    /**
     * <커플의 전체 꿀 개수 조회>
     *     - 파라미터에 따라 User, Couple, Calendar 조회 -> 에러 반환
     *     - 커플이 가진 전체 꿀 개수 반환
     */
    @Transactional(readOnly = true)
    public ApiResponseDto<Long> getTotalHoneyByUser(UserDetails userDetails){

        final User user = userService.getUser(userDetails);
        final Couple couple = coupleService.getCoupleByUser(user);

        return ResponseUtils.ok(
                couple.getTotal_honey(),
                ErrorResponse.builder().status(200).message("요청 성공").build()
        );
    }

    /**
     * <커플의 월별 꿀 개수 조회>
     *     - 파라미터에 따라 User, Couple, List<Calendar> 조회 -> 에러 반환
     *     - 커플이 가진 월별 꿀 개수 반환
     *     - 해당 년/월에 만들어진 memory가 없으면 0 반환
     */
    @Transactional(readOnly = true)
    public ApiResponseDto<Integer> getMonthlyHoneyByUser(UserDetails userDetails, int year, int month) {

        final User user = userService.getUser(userDetails);
        final Couple couple = coupleService.getCoupleByUser(user);
        List<Calendar> calendars = calendarRepository.findAllByCoupleAndYearAndMonth(couple, year, month);

        int monthlyHoney = 0;
        if (!calendars.isEmpty()) {     // calendar 리스트가 비어있지 않으면 월별 honey 개수 세기
            for (Calendar calendar : calendars) {
                monthlyHoney += calendar.getCalendarMemories().size();     // 현재 Calendar의 CalendarMemory 개수를 추가하여 누적
            }
        }

        return ResponseUtils.ok(
                monthlyHoney,
                ErrorResponse.builder().status(200).message("요청 성공").build()
        );
    }

}