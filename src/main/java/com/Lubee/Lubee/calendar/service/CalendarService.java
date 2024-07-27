package com.Lubee.Lubee.calendar.service;

import com.Lubee.Lubee.calendar.domain.Calendar;
import com.Lubee.Lubee.calendar.repository.CalendarRepository;
import com.Lubee.Lubee.common.api.ApiResponseDto;
import com.Lubee.Lubee.common.api.ErrorResponse;
import com.Lubee.Lubee.common.api.ResponseUtils;
import com.Lubee.Lubee.common.enumSet.ErrorType;
import com.Lubee.Lubee.common.exception.RestApiException;
import com.Lubee.Lubee.couple.domain.Couple;
import com.Lubee.Lubee.couple.repository.CoupleRepository;
import com.Lubee.Lubee.user.domain.User;
import com.Lubee.Lubee.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CalendarService {

    public int getCalendarMonth(int month) {
        return switch (month) {
            case 1 -> java.util.Calendar.JANUARY;
            case 2 -> java.util.Calendar.FEBRUARY;
            case 3 -> java.util.Calendar.MARCH;
            case 4 -> java.util.Calendar.APRIL;
            case 5 -> java.util.Calendar.MAY;
            case 6 -> java.util.Calendar.JUNE;
            case 7 -> java.util.Calendar.JULY;
            case 8 -> java.util.Calendar.AUGUST;
            case 9 -> java.util.Calendar.SEPTEMBER;
            case 10 -> java.util.Calendar.OCTOBER;
            case 11 -> java.util.Calendar.NOVEMBER;
            case 12 -> java.util.Calendar.DECEMBER;
            default -> throw new IllegalArgumentException("Invalid month: " + month);
        };
    }

}
