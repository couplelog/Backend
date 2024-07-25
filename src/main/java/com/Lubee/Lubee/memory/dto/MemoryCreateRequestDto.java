package com.Lubee.Lubee.memory.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Getter
public class MemoryCreateRequestDto {
    private  Long location_id;
    private MultipartFile picture;
    private int year;
    private int month;
    private int day;
}
