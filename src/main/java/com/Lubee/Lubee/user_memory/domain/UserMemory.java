package com.Lubee.Lubee.user_memory.domain;

import com.Lubee.Lubee.memory.domain.Memory;
import com.Lubee.Lubee.user.domain.User;
import com.Lubee.Lubee.user_calendar_memory.domain.UserCalendarMemory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class UserMemory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "memory_id")
    private Memory memory;

    public UserMemory(User user, Memory memory) {
        this.user = user;
        this.memory = memory;
    }

    // of 메서드 정의
    public static UserMemory of(User user, Memory memory) {
        return new UserMemory(user, memory);
    }
}
