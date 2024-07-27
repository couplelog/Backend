package com.Lubee.Lubee.memory.facade;

import com.Lubee.Lubee.calendar.repository.CalendarRepository;
import com.Lubee.Lubee.calendar.service.CalendarService;
import com.Lubee.Lubee.common.api.ApiResponseDto;
import com.Lubee.Lubee.common.api.ErrorResponse;
import com.Lubee.Lubee.common.api.ResponseUtils;
import com.Lubee.Lubee.common.api.SuccessResponse;
import com.Lubee.Lubee.common.enumSet.ErrorType;
import com.Lubee.Lubee.common.exception.RestApiException;
import com.Lubee.Lubee.couple.domain.Couple;
import com.Lubee.Lubee.couple.repository.CoupleRepository;
import com.Lubee.Lubee.couple.service.CoupleService;
import com.Lubee.Lubee.date_comment.service.DateCommentService;
import com.Lubee.Lubee.memory.domain.Memory;
import com.Lubee.Lubee.memory.dto.HomeDto;
import com.Lubee.Lubee.memory.dto.MemoryBaseDto;
import com.Lubee.Lubee.memory.repository.MemoryRepository;
import com.Lubee.Lubee.memory.service.MemoryService;
import com.Lubee.Lubee.user.domain.User;
import com.Lubee.Lubee.user.service.UserService;
import com.Lubee.Lubee.user_calendar_memory.repository.UserCalendarMemoryRepository;
import com.Lubee.Lubee.user_memory_reaction.repository.UserMemoryReactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemoryFacade {

    private final MemoryService memoryService;
    private final CalendarService calendarService;
    private final UserService userService;
    private final CoupleService coupleService;
    private final DateCommentService dateCommentService;
    private final MemoryRepository memoryRepository;
    private final CalendarRepository calendarRepository;
    private final UserMemoryReactionRepository userMemoryReactionRepository;
    private final UserCalendarMemoryRepository userCalendarMemoryRepository;
    private final CoupleRepository coupleRepository;
    @Transactional
    public ApiResponseDto<HomeDto> getHomeInfo(UserDetails loginUser) {
        try {
            // 유저 정보로 커플 정보 얻기
            User user = userService.getUser(loginUser);
            Couple couple = coupleService.getCoupleByUser(user);

            String today_date_str = memoryService.getToday();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd");

            // 문자열을 Date 객체로 변환
            Date today_date = formatter.parse(today_date_str);

            // memory에서 정보 가져오기
            List<Memory> memoryList = memoryService.getMemorybyUserAndDate(today_date, couple);

            // 커플 사귄날짜를 이용하여 연애일 수 가져오기
            long loveDays = memoryService.getLoveDays(couple.getStartDate());

            // 유저의 꿀 개수 계산
            int honey = memoryList.size();

            // HomeDto 객체 생성 및 설정
            HomeDto homeDto = new HomeDto(loveDays);

            // ApiResponseDto 객체 반환
            return ResponseUtils.ok(homeDto, null);

        } catch (ParseException e) {
            // ParseException 발생 시 처리 로직
            e.printStackTrace(); // 로깅

            // ErrorResponse를 통해 예외 상황을 클라이언트에게 알림
            return ResponseUtils.error(ErrorResponse.of(ErrorType.PARSING_ERROR));
        } catch (Exception e) {
            // 그 외 예외 발생 시 처리 로직
            e.printStackTrace(); // 로깅

            // ErrorResponse를 통해 예외 상황을 클라이언트에게 알림
            return ResponseUtils.error(ErrorResponse.of(ErrorType.INTERNAL_SERVER_ERROR));
        }
    }

    public ApiResponseDto<SuccessResponse> createMemory(UserDetails loginUser, MultipartFile file, Long location_id, int year, int month, int day)
    {

        // memory 생성, calendar 도 생성, memory_calendar도 생성해준다
        User user = userService.getUser(loginUser);
        Couple couple = coupleService.getCoupleByUser(user);
        couple.setTotal_honey(couple.getTotal_honey()+1);
        coupleRepository.save(couple);
        memoryService.createMemory(loginUser, file,location_id, year, month, day);
        return ResponseUtils.ok(SuccessResponse.of(HttpStatus.OK, "Memory 생성이 완료되었습니다"), ErrorResponse.builder().status(200).message("요청 성공").build());
    }

    public ApiResponseDto<MemoryBaseDto> getOneMemory(UserDetails loginUser, Long memoryId)
    {
        MemoryBaseDto memoryBaseDto = memoryService.getOneMemory(loginUser, memoryId);
        return ResponseUtils.ok(memoryBaseDto, null);
    }

    public ApiResponseDto<SuccessResponse> deleteMemory(UserDetails loginUser, Long memoryId)
    {
        User user = userService.getUser(loginUser);
        Couple couple = coupleService.getCoupleByUser(user);
        couple.setTotal_honey(couple.getTotal_honey()-1);
        coupleRepository.save(couple);
        Memory memory = memoryRepository.findById(memoryId).orElseThrow(
                () -> new RestApiException(ErrorType.NOT_FOUND)
        );
        memoryRepository.delete(memory);
        return ResponseUtils.ok(SuccessResponse.of(HttpStatus.OK, "해당 Memory 삭제가 완료되었습니다"), ErrorResponse.builder().status(200).message("요청 성공").build());

    }


}
