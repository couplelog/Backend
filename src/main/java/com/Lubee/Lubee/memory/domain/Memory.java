package com.Lubee.Lubee.memory.domain;

import com.Lubee.Lubee.calendar_memory.domain.CalendarMemory;
import com.Lubee.Lubee.common.BaseEntity;
import com.Lubee.Lubee.couple.domain.Couple;
import com.Lubee.Lubee.location.domain.Location;
import com.Lubee.Lubee.user.domain.User;
import com.Lubee.Lubee.user_memory_reaction.domain.UserMemoryReaction;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Memory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "memory_id")
    private Long memory_id;

    @Column(nullable = true)
    private String content;

    @DateTimeFormat(pattern = "yyyy.MM.dd")
    @Temporal(TemporalType.DATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd", timezone = "Asia/Seoul")
    private Date time;

    private String picture;

    @ManyToOne
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @ManyToOne
    @JoinColumn(name = "writer_id", nullable = false)
    private User writer;  // 다대일 관계 설정

    @ManyToOne
    @JoinColumn(name = "couple_id", nullable = false)
    private Couple couple;

    @OneToMany(mappedBy = "memory", cascade = CascadeType.ALL)
    private List<CalendarMemory> calendarMemories = new ArrayList<>();

    @OneToMany(mappedBy = "memory", cascade = CascadeType.ALL)
    private List<UserMemoryReaction> userMemoryReactions = new ArrayList<>();

}