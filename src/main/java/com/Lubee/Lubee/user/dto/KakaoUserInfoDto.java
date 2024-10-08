package com.Lubee.Lubee.user.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Getter
public class KakaoUserInfoDto {
    private String kakaoId;
    private String nickname;
}
