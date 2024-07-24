package com.Lubee.Lubee.date_comment.repository;

import com.Lubee.Lubee.calendar.domain.Calendar;
import com.Lubee.Lubee.couple.domain.Couple;
import com.Lubee.Lubee.date_comment.domain.DateComment;
import com.Lubee.Lubee.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface DateCommentRepository extends JpaRepository<DateComment, Long> {

    DateComment findByUserAndCalendar(User user, Calendar calendar);
    List<DateComment> findByCoupleAndCalendar(Couple couple, Calendar calendar);

    @Query("SELECT dc FROM DateComment dc WHERE dc.couple = :couple AND dc.calendar.eventDate = :eventDate")
    List<DateComment> findByCoupleAndCalendarEventDate(@Param("couple") Couple couple, @Param("eventDate") Date eventDate);

    // 유저가해당 날짜에 이미 데이트코멘트를 작성했는지 확인
    Optional<DateComment> findByUserAndCoupleAndCalendar_EventDate(User user, Couple couple, Date eventDate);


}