package com.Lubee.Lubee.anniversary.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Setter
@Getter
@AllArgsConstructor
@ToString
public class AnniversaryDto {

    private String anniversary_title;

    @JsonFormat(pattern = "yyyy.MM.dd")
    private Date anniversary_date;

    public static AnniversaryDto of(String anniversary_title, Date anniversary_date) {

        return new AnniversaryDto(anniversary_title, anniversary_date);
    }
}