package com.Lubee.Lubee.couple.dto;

import com.Lubee.Lubee.anniversary.dto.AnniversaryListDto;
import com.Lubee.Lubee.enumset.Profile;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.util.Date;

@Getter
@AllArgsConstructor
@ToString
public class CoupleInfoDto{

    private String nickname_first;
    private Profile profile_first;
    private String nickname_second;
    private Profile profile_second;
    @JsonFormat(pattern = "yyyy.MM.dd")
    private Date birthday_first;
    @JsonFormat(pattern = "yyyy.MM.dd")
    private Date birthday_second;
    private AnniversaryListDto anniversaryListDto;


    public static CoupleInfoDto of(String nickname_first, Profile profile_first, String nickname_second, Profile profile_second,Date birthday_first, Date birthday_seond, AnniversaryListDto anniversaryListDto) {

        return new CoupleInfoDto(nickname_first, profile_first, nickname_second, profile_second, birthday_first, birthday_seond, anniversaryListDto);
    }

}