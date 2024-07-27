package com.Lubee.Lubee.calendar.controller;

import com.Lubee.Lubee.calendar.service.HoneyService;
import com.Lubee.Lubee.common.api.ApiResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/calendars/honey")
public class HoneyController {

    private final HoneyService honeyService;

    /**
     * 오늘의 꿀 조회
     *
     * @param userDetails 인증된 사용자의 정보를 담고 있는 UserDetails 객체
     * @param date        꿀 정보 얻기를 원하는 날짜
     * @return ApiResponseDto<Integer>  해당 날짜의 꿀 개수
     */
    @GetMapping("/today")
    public ApiResponseDto<Integer> getTodayHoney(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @DateTimeFormat(pattern = "yyyy.MM.dd") final Date date) {

        return honeyService.getHoneyInfoByUserAndDate(userDetails, date);
    }

    /**
     * 커플의 전체 꿀 조회
     *
     * @param userDetails 인증된 사용자의 정보를 담고 있는 UserDetails 객체
     * @return ApiResponseDto<Long>  커플이 가진 전체 꿀 개수
     */
    @GetMapping("/total")
    public ApiResponseDto<Long> getTotalHoney(
            @AuthenticationPrincipal UserDetails userDetails) {

        return honeyService.getTotalHoneyByUser(userDetails);
    }

    /**
     * 커플의 월별 꿀 조회
     *
     * @return ApiResponseDto<Integer>  커플이 가진 전체 꿀 개수
     */
    @GetMapping("/month")
    public ApiResponseDto<Integer> getMonthHoney(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam int year,
            @RequestParam int month) {

        return honeyService.getMonthlyHoneyByUser(userDetails, year, month);
    }

}
