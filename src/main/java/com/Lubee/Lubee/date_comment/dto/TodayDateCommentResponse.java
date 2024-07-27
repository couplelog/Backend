package com.Lubee.Lubee.date_comment.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor // 기본 생성자 추가
@ToString
public class TodayDateCommentResponse {

    private String comment_first;
    private String comment_second;

    public TodayDateCommentResponse(String comment_first, String comment_second) {
        this.comment_first = comment_first;
        this.comment_second = comment_second;
    }

}
