package com.Lubee.Lubee.calendar_memory.service;

import com.Lubee.Lubee.calendar.dto.CalendarMemoryDayDto;
import com.Lubee.Lubee.calendar.dto.CalendarMemoryTotalListDto;
import com.Lubee.Lubee.calendar.dto.CalendarMemoryYearMonthDto;
import com.Lubee.Lubee.calendar.repository.CalendarRepository;
import com.Lubee.Lubee.calendar_memory.domain.CalendarMemory;
import com.Lubee.Lubee.calendar_memory.repository.CalendarMemoryRepository;
import com.Lubee.Lubee.common.enumSet.ErrorType;
import com.Lubee.Lubee.common.exception.RestApiException;
import com.Lubee.Lubee.couple.domain.Couple;
import com.Lubee.Lubee.couple.repository.CoupleRepository;
import com.Lubee.Lubee.enumset.Profile;
import com.Lubee.Lubee.enumset.Reaction;
import com.Lubee.Lubee.location.domain.Location;
import com.Lubee.Lubee.location.repository.LocationRepository;
import com.Lubee.Lubee.memory.domain.Memory;
import com.Lubee.Lubee.memory.dto.MemoryBaseDto;
import com.Lubee.Lubee.user.domain.User;
import com.Lubee.Lubee.user.repository.UserRepository;
import com.Lubee.Lubee.user_calendar_memory.repository.UserCalendarMemoryRepository;
import com.Lubee.Lubee.user_memory_reaction.domain.UserMemoryReaction;
import com.Lubee.Lubee.user_memory_reaction.repository.UserMemoryReactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CalendarMemoryService {

    private final CalendarRepository calendarRepository;
    private final CalendarMemoryRepository calendarMemoryRepository;
    private final CoupleRepository coupleRepository;
    private final UserRepository userRepository;
    private final UserMemoryReactionRepository userMemoryReactionRepository;
    private final UserCalendarMemoryRepository userCalendarMemoryRepository;
    private final LocationRepository locationRepository;

    public CalendarMemoryTotalListDto getYearlyMonthlyCalendarInfo(UserDetails loginUser) {
        User user = userRepository.findByUsername(loginUser.getUsername())
                .orElseThrow(() -> new RestApiException(ErrorType.NOT_FOUND_USER));
        Couple couple = coupleRepository.findCoupleByUser(user)
                .orElseThrow(() -> new RestApiException(ErrorType.NOT_FOUND_COUPLE));

        Date startDate = couple.getStartDate();
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(startDate);
        int startYear = startCalendar.get(Calendar.YEAR);
        int startMonth = startCalendar.get(Calendar.MONTH);

        Calendar todayCalendar = Calendar.getInstance();
        int todayYear = todayCalendar.get(Calendar.YEAR);
        int todayMonth = todayCalendar.get(Calendar.MONTH);

        List<CalendarMemoryYearMonthDto> yearMonthDtoList = new ArrayList<>();

        for (int year = startYear; year <= todayYear; year++) {
            int start = (year == startYear) ? startMonth : 0;
            int end = (year == todayYear) ? todayMonth : 11;

            for (int month = start; month <= end; month++) {
                List<CalendarMemoryDayDto> dayDtoListForMonth = new ArrayList<>();
                Calendar monthCalendar = Calendar.getInstance();
                monthCalendar.set(year, month, 1);
                int daysInMonth = monthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);

                for (int day = 1; day <= daysInMonth; day++) {
                    List<MemoryBaseDto> memoryBaseDtoList = new ArrayList<>();

                    List<CalendarMemory> calendarMemoryList = calendarMemoryRepository.findAllByCoupleAndYearAndMonthAndDay(couple, year, month + 1, day);
                    if (calendarMemoryList != null) {
                        for (CalendarMemory calendarMemory : calendarMemoryList) {
                            Memory memory = calendarMemory.getMemory();

                            Optional<UserMemoryReaction> optional_reaction_first, optional_reaction_second;
                            Reaction reaction_first = null;
                            Reaction reaction_second = null;

                            // 애인 찾기
                            User user_second = findOtherUserInCouple(user.getId(), couple);

                            //리액션 지정
                            optional_reaction_first = userMemoryReactionRepository.findByUserAndMemory(user, memory);
                            optional_reaction_second = userMemoryReactionRepository.findByUserAndMemory(user_second, memory);
                            if (optional_reaction_first.isPresent())
                                reaction_first = optional_reaction_first.get().getReaction();
                            if (optional_reaction_second.isPresent())
                                reaction_second = optional_reaction_second.get().getReaction();

                            Location location = locationRepository.findById(memory.getLocation().getLocation_id())
                                    .orElseThrow(() -> new RestApiException(ErrorType.NOT_FOUND_LOCATION));
                            String locationName = location.getName();
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH시-mm분");
                            String upload_time = memory.getCreatedDate().format(formatter);
                            MemoryBaseDto memoryBaseDto = MemoryBaseDto.of(
                                    memory.getMemory_id(),
                                    locationName,
                                    memory.getPicture(),
                                    memory.getUserMemory().getUser().getProfile(),
                                    reaction_first,
                                    reaction_second,
                                    upload_time
                            );
                            memoryBaseDtoList.add(memoryBaseDto);
                        }
                    }

                    if (!memoryBaseDtoList.isEmpty()) {
                        CalendarMemoryDayDto calendarMemoryDayDto = CalendarMemoryDayDto.of(day, memoryBaseDtoList);
                        dayDtoListForMonth.add(calendarMemoryDayDto);
                    }
                }

                CalendarMemoryYearMonthDto yearMonthDto = CalendarMemoryYearMonthDto.of(year, month + 1, dayDtoListForMonth);
                yearMonthDtoList.add(yearMonthDto);
            }
        }

        return CalendarMemoryTotalListDto.of(yearMonthDtoList);
    }

    public CalendarMemoryDayDto getDayCalendarInfo(UserDetails loginUser, int year, int month, int day) {
        User user = userRepository.findByUsername(loginUser.getUsername())
                .orElseThrow(() -> new RestApiException(ErrorType.NOT_FOUND_USER));
        Couple couple = coupleRepository.findCoupleByUser(user)
                .orElseThrow(() -> new RestApiException(ErrorType.NOT_FOUND_COUPLE));
        User user_second = userRepository.findRestUser(user, couple).orElseThrow(
                () -> new RestApiException(ErrorType.NOT_FOUND_USER)
        );
        List<MemoryBaseDto> memoryBaseDtoList = new ArrayList<>();

        // 해당 날짜에 맞는 CalendarMemory 리스트를 가져옵니다.
        List<CalendarMemory> calendarMemoryList = calendarMemoryRepository.findAllByCoupleAndYearAndMonthAndDay(couple, year, month, day);
        System.out.println("calendar memoryList : " + calendarMemoryList);
        if (calendarMemoryList != null) {
            for (CalendarMemory calendarMemory : calendarMemoryList) {
                Memory memory = calendarMemory.getMemory();
                System.out.println("memoru_id :" + memory.getMemory_id());
                Reaction reaction_first = null;
                Reaction reaction_second = null;
                Profile profile = null;
                Optional<UserMemoryReaction> optional_reaction_first, optional_reaction_second;
                optional_reaction_first = userMemoryReactionRepository.findByUserAndMemory(user, memory);
                optional_reaction_second = userMemoryReactionRepository.findByUserAndMemory(user_second, memory);
                if (optional_reaction_first.isPresent())
                    reaction_first = optional_reaction_first.get().getReaction();
                if (optional_reaction_second.isPresent())
                    reaction_second = optional_reaction_second.get().getReaction();

                Location location = locationRepository.findById(memory.getLocation().getLocation_id())
                        .orElseThrow(() -> new RestApiException(ErrorType.NOT_FOUND_LOCATION));
                String locationName = location.getName();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH시-mm분");
                String upload_time = memory.getCreatedDate().format(formatter);
                MemoryBaseDto memoryBaseDto = MemoryBaseDto.of(
                        memory.getMemory_id(),
                        locationName,
                        memory.getPicture(),
                        memory.getUserMemory().getUser().getProfile(),
                        reaction_first,
                        reaction_second,
                        upload_time
                );
                memoryBaseDtoList.add(memoryBaseDto);
            }
        }

        return CalendarMemoryDayDto.of(day, memoryBaseDtoList);
    }

    // 다른 유저 찾기
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
}
