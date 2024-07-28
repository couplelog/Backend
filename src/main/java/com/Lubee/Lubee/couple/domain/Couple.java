package com.Lubee.Lubee.couple.domain;

import com.Lubee.Lubee.anniversary.domain.Anniversary;
import com.Lubee.Lubee.calendar.domain.Calendar;
import com.Lubee.Lubee.date_comment.domain.DateComment;
import com.Lubee.Lubee.user.domain.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Couple {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "couple_id")
    private Long id;

    @DateTimeFormat(pattern = "yyyy.MM.dd")
    @Temporal(TemporalType.DATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd", timezone = "Asia/Seoul")
    private Date startDate;

    private boolean subscribe;

    private Long total_honey;

    private int present_honey;

    @OneToMany(mappedBy = "couple", cascade = CascadeType.ALL)      // couple 삭제 -> user 즉시 변동
    private List<User> user = new ArrayList<>();

    @OneToMany(mappedBy = "couple", cascade = CascadeType.ALL, orphanRemoval = true)  // cascade 추가
    private List<Calendar> calendars = new ArrayList<>();

    @OneToMany(mappedBy = "couple")
    private List<DateComment> dateComments = new ArrayList<>();

    @OneToMany(mappedBy = "couple")
    private List<Anniversary> anniversaries = new ArrayList<>();

    @Builder
    public Couple(User requester, User receiver) {
        user.add(requester);
        user.add(receiver);
        this.subscribe = false;
        this.total_honey = 0L;
        this.present_honey = 0;
    }

    @PreRemove
    public void onPreRemove() {             // couple이 삭제될 때 user의 값 자동 변경
        for (User user : user) {
            user.setCouple(null);
            user.setAlreadyCouple(false);
        }
    }

    public void subtractTotalHoney() {      // totalHoney 빼기 (memory 삭제)
        this.total_honey--;
    }

    public void addTotalHoney() {           // totalHoney 더하기 (memory 추가)
        this.total_honey++;
    }

    public void setting_start(Couple couple, Date startDate)
    {
        this.startDate = startDate;
    }

}