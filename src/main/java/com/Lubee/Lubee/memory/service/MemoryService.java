package com.Lubee.Lubee.memory.service;

import com.Lubee.Lubee.calendar.domain.Calendar;
import com.Lubee.Lubee.calendar.repository.CalendarRepository;
import com.Lubee.Lubee.calendar.service.CalendarService;
import com.Lubee.Lubee.calendar_memory.domain.CalendarMemory;
import com.Lubee.Lubee.calendar_memory.repository.CalendarMemoryRepository;
import com.Lubee.Lubee.common.enumSet.ErrorType;
import com.Lubee.Lubee.common.exception.RestApiException;
import com.Lubee.Lubee.couple.domain.Couple;
import com.Lubee.Lubee.couple.repository.CoupleRepository;
import com.Lubee.Lubee.couple.service.CoupleService;
import com.Lubee.Lubee.enumset.Profile;
import com.Lubee.Lubee.enumset.Reaction;
import com.Lubee.Lubee.location.domain.Location;
import com.Lubee.Lubee.location.repository.LocationRepository;
import com.Lubee.Lubee.location.service.LocationService;
import com.Lubee.Lubee.memory.domain.Memory;
import com.Lubee.Lubee.memory.dto.MemoryBaseDto;
import com.Lubee.Lubee.memory.dto.MemoryCreateRequestDto;
import com.Lubee.Lubee.memory.repository.MemoryRepository;
import com.Lubee.Lubee.user.domain.User;
import com.Lubee.Lubee.user.repository.UserRepository;
import com.Lubee.Lubee.user_memory_reaction.domain.UserMemoryReaction;
import com.Lubee.Lubee.user_memory_reaction.repository.UserMemoryReactionRepository;
import com.Lubee.Lubee.user_memory_reaction.service.UserMemoryReactionService;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemoryService {

    private final MemoryRepository memoryRepository;
    private final UserMemoryReactionRepository userMemoryReactionRepository;
    private final AmazonS3Client amazonS3Client;
    private final UserRepository userRepository;
    private final CoupleRepository coupleRepository;
    private final CalendarRepository calendarRepository;
    private final CalendarMemoryRepository calendarMemoryRepository;
    private final LocationRepository locationRepository;
    private final CalendarService calendarService;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    public String getToday()
    {
        Date today = new Date();

        // 날짜 포맷 정의하기
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd");

        // 포맷에 맞춰 날짜를 문자열로 변환하기
        return formatter.format(today);
    }

    public Long getLoveDays(Date startDate) {

        // java.sql.Date를 java.util.Date로 변환
        java.util.Date utilDate = new java.util.Date(startDate.getTime());

        LocalDate specificDate = utilDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalDate today = LocalDate.now();

        // 날짜 차이 계산하기
        return ChronoUnit.DAYS.between(specificDate, today);

    }

//    public List<MemoryBaseDto> getMemoryBase(List<UserCalendarMemory> userCalendarMemories) {
//        ArrayList<MemoryBaseDto> memoryBaseDtoArrayList = new ArrayList<>();
//
//        for (UserCalendarMemory userCalendarMemory : userCalendarMemories) {
//            Memory memory = userCalendarMemory.getCalendarMemory().getMemory();
//            String location_name = locationService.getLocationByMemory(memory).getName();
//            String picture = locationService.getLocationByMemory(memory).getPicture();
//            List<UserMemoryReaction> userMemoryReactionList = userMemoryReactionService.getUserMemoryReactionsByMemory(memory);
//
//            int i = 0;
//            Reaction reaction1 = null;
//            Reaction reaction2 = null;
//            Profile profile = null;
//
//            for (UserMemoryReaction userMemoryReaction : userMemoryReactionList) {
//                if (i == 0) {
//                    reaction1 = userMemoryReaction.getReaction();
//                    i++;
//                } else if (i == 1) {
//                    reaction2 = userMemoryReaction.getReaction();
//                    profile = userMemoryReaction.getUser().getProfile();
//                    i++;
//                }
//            }
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH시-mm분");
//            String upload_time = memory.getCreatedDate().format(formatter);
//            MemoryBaseDto memoryBaseDto = MemoryBaseDto.of(memory.getMemory_id(), location_name, picture, profile, reaction1, reaction2, upload_time);
//            memoryBaseDtoArrayList.add(memoryBaseDto);
//        }
//
//        return memoryBaseDtoArrayList;
//    }

    @Transactional
    public void createMemory(User user, MultipartFile file, Long location_id, int year, int month, int day) {

        // 사용자 정보 가져오기
//        User user = userRepository.findByUsername(loginUser.getUsername()).orElseThrow(
//                () -> new RestApiException(ErrorType.NOT_FOUND_USER)
//        );
        Couple couple = coupleRepository.findCoupleByUser(user).orElseThrow(
                () -> new RestApiException(ErrorType.NOT_FOUND_COUPLE)
        );

        // 파일 업로드 처리
        if (file != null && !file.isEmpty()) {
            try {
                List<Memory> memoryList = memoryRepository.findAllByCoupleAndYearAndMonthAndDay(couple, year, month, day);
                if (memoryList.size() >=5)
                {
                    throw new RestApiException(ErrorType.TODAY_MEMORY_END);
                }
                String originalFilename = file.getOriginalFilename();
                String fileExtension = "";
                assert originalFilename != null;
                int dotIndex = originalFilename.lastIndexOf('.');
                if (dotIndex > 0) {
                    fileExtension = originalFilename.substring(dotIndex);
                }
                String baseFileName = originalFilename.substring(0, dotIndex);

                // 현재 날짜와 시간으로 타임스탬프 생성
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
                String timestamp = now.format(formatter);

                // 새로운 파일 이름 생성
                String fileName = baseFileName + "_" + timestamp + fileExtension;
                String folder = "/pictures"; // 저장할 폴더
                String fileUrl = "https://" + bucket + ".s3." + region + ".amazonaws.com" + folder + "/" + fileName;

                // S3에 파일 업로드
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentType(file.getContentType());
                metadata.setContentLength(file.getSize());
                amazonS3Client.putObject(bucket + folder, fileName, file.getInputStream(), metadata);

                // Location 찾기
                Location location = locationRepository.findById(location_id).orElseThrow(
                        () -> new RestApiException(ErrorType.NOT_FOUND_LOCATION)
                );

                int calenderMonth = calendarService.getCalendarMonth(month);
                java.util.Calendar calendar_type = java.util.Calendar.getInstance();
                calendar_type.set(year, calenderMonth, day); // month - 1: 0-based month 사용
                Date date = calendar_type.getTime();
                // 메모리 객체 생성 및 저장
                Memory memory = new Memory();
                memory.setLocation(location);
                memory.setTime(date); // 현재 날짜 설정
                memory.setPicture(fileUrl);
                memory.setCouple(couple);
                memory.setWriter(user);
                memory = memoryRepository.save(memory);

                // Calendar 존재 확인 및 생성
                Calendar calendar = calendarRepository.findByCoupleAndEventDate(couple, date);
                if (calendar == null) {
                    calendar = Calendar.builder()
                            .couple(couple)
                            .eventDate(date)
                            .build();
                    calendar = calendarRepository.save(calendar);
                }

                // CalendarMemory 생성 및 저장
                CalendarMemory calendarMemory = CalendarMemory.builder()
                        .calendar(calendar)
                        .memory(memory)
                        .build();
                calendarMemoryRepository.save(calendarMemory);
                coupleRepository.save(couple);

            } catch (IOException e) {
                e.printStackTrace();
                throw new RestApiException(ErrorType.FILE_UPLOAD_FAILED);
            }
        } else {
            throw new RestApiException(ErrorType.FILE_NOT_PROVIDED);
        }
    }

    @Transactional
    public MemoryBaseDto getOneMemory(UserDetails  loginUser,  Long memoryId) {

        User user = userRepository.findByUsername(loginUser.getUsername()).orElseThrow(
                () -> new RestApiException(ErrorType.NOT_FOUND));

        Memory memory = memoryRepository.findById(memoryId).orElseThrow(
                () -> new RestApiException(ErrorType.NOT_FOUND));
        Optional<UserMemoryReaction> optional_reaction_first, optional_reaction_second;
        Reaction reaction_first = null;
        Reaction reaction_second = null;

        // 애인 찾기
        Optional<Couple> optionalCouple = coupleRepository.findCoupleByUser(user);

        User user_second;

        // 리액션 지정
        if(optionalCouple.isPresent()) {
            Couple couple = optionalCouple.get();
            user_second = findOtherUserInCouple(user.getId(), couple);

            optional_reaction_first = userMemoryReactionRepository.findByUserAndMemory(user, memory);
            optional_reaction_second = userMemoryReactionRepository.findByUserAndMemory(user_second, memory);
            if (optional_reaction_first.isPresent())
                reaction_first = optional_reaction_first.get().getReaction();
            if (optional_reaction_second.isPresent())
                reaction_second = optional_reaction_second.get().getReaction();
        }
        else{       // 애인이 없을 경우
            user_second = null;
        }
        Profile writer_profile_first = null;
        Profile writer_profile_second = null;
        // MemoryBaseDto 생성
        if (user == memory.getWriter())
        {
            writer_profile_first = memory.getWriter().getProfile();
        }
        else {
            assert user_second != null;
            writer_profile_second = user_second.getProfile();
        }
        Location location = memory.getLocation();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH시-mm분");
        String upload_time = memory.getCreatedDate().format(formatter);
        return MemoryBaseDto.of(
                memory.getMemory_id(),
                location.getName(),
                memory.getPicture(),
                writer_profile_first,
                writer_profile_second,
                reaction_first,
                reaction_second,
                upload_time
        );
    }

    @Transactional
    public List<Memory> getMemorybyUserAndDate(Date date, Couple couple) {
        return memoryRepository.findByMemoryAndUser(date, couple);
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

    public void deleteS3(Memory memory) {
        String url = memory.getPicture();

        // URL에서 파일명 추출
        String[] parts = url.split("/");  // URL을 '/'로 나누기
        String fileName = parts[parts.length - 1];  // 마지막 부분을 가져오기

        // S3 객체의 키를 생성
        String key = "pictures/" + fileName;  // S3에서 객체의 키를 구성합니다. 'pictures/' 폴더는 URL의 폴더 구조와 일치해야 합니다.

        try {
            // S3에서 객체 삭제
            amazonS3Client.deleteObject(bucket, key);
            System.out.println("Deleted S3 object with key: " + key);
        } catch (AmazonS3Exception e) {
            log.debug("Error deleting S3 object: " + e);
        }
    }



}
