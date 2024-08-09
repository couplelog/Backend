package com.Lubee.Lubee.calendar.controller;

import com.Lubee.Lubee.calendar.dto.CalendarMemoryDayDto;
import com.Lubee.Lubee.calendar.dto.CalendarMemoryTotalListDto;
import com.Lubee.Lubee.calendar.repository.CalendarRepository;
import com.Lubee.Lubee.calendar.service.CalendarService;
import com.Lubee.Lubee.calendar_memory.service.CalendarMemoryService;
import com.Lubee.Lubee.common.api.ApiResponseDto;
import com.Lubee.Lubee.common.api.ErrorResponse;
import com.Lubee.Lubee.common.api.ResponseUtils;
import com.Lubee.Lubee.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/calendars")
public class CalendarController {

    private final CalendarMemoryService calendarMemoryService;


    /**
     * year과 month를 검색하였을 때 year와 month가 일치하는 usermemory의 id를 가져와서 반환해준다. 이때의 day도 반환해준다
     */
    @GetMapping("/total_calendar")
    public ApiResponseDto<CalendarMemoryTotalListDto> getTotalCalendar(
            @AuthenticationPrincipal UserDetails userDetails)
    {
        CalendarMemoryTotalListDto totalCalendarDtoList = calendarMemoryService.getYearlyMonthlyCalendarInfo(userDetails);
        return ResponseUtils.ok(totalCalendarDtoList, ErrorResponse.builder().status(200).message("요청 성공").build());
    }

    @GetMapping("/specific_calendar")
    public ApiResponseDto<CalendarMemoryDayDto> getSpecificCalendar(
            @AuthenticationPrincipal UserDetails userDetails, @RequestParam int year, @RequestParam int month, @RequestParam int day)
    {
        CalendarMemoryDayDto calendarMemoryDayDto = calendarMemoryService.getDayCalendarInfo(userDetails, year, month, day);
        return ResponseUtils.ok(calendarMemoryDayDto, ErrorResponse.builder().status(200).message("요청 성공").build());
    }

}
