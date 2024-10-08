package com.Lubee.Lubee.calendar.controller;

import com.Lubee.Lubee.calendar.dto.MonthlyTotalHoneyRequest;
import com.Lubee.Lubee.calendar.service.CalendarService;
import com.Lubee.Lubee.common.api.ApiResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/calendars")
public class CalendarController {

    private final CalendarService calendarService;

    /**
     * 오늘의 꿀 조회
     *
     * @param userDetails 인증된 사용자의 정보를 담고 있는 UserDetails 객체
     * @param date 꿀 정보 얻기를 원하는 날짜
     * @return ApiResponseDto<Integer>  해당 날짜의 꿀 개수
     */
    @GetMapping("/honey/today")
    public ApiResponseDto<Integer> getTodayHoney(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam final Date date) {

        return calendarService.getHoneyInfoByUserAndDate(userDetails, date);
    }

    /**
     * 커플의 전체 꿀 조회
     *
     * @param userDetails 인증된 사용자의 정보를 담고 있는 UserDetails 객체
     * @return ApiResponseDto<Long>  커플이 가진 전체 꿀 개수
     */
    @GetMapping("/honey/total")
    public ApiResponseDto<Long> getTotalHoney(@AuthenticationPrincipal UserDetails userDetails){

        return calendarService.getTotalHoneyByUser(userDetails);
    }

    /**
     * 커플의 월별 꿀 조회
     *
     * @param userDetails 인증된 사용자의 정보를 담고 있는 UserDetails 객체
     * @param monthlyTotalHoneyRequest 원하는 년/월을 integer 값으로
     * @return ApiResponseDto<Integer>  커플이 가진 전체 꿀 개수
     */
    @PostMapping("/honey/month")
    public ApiResponseDto<Integer> getMonthHoney(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody final MonthlyTotalHoneyRequest monthlyTotalHoneyRequest){

        return calendarService.getMonthlyHoneyByUser(userDetails, monthlyTotalHoneyRequest);
    }
}
