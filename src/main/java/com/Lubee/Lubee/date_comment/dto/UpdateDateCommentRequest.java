package com.Lubee.Lubee.date_comment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Getter
@NoArgsConstructor
public class UpdateDateCommentRequest {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd")
    private Date date;
    private String content;

    public UpdateDateCommentRequest(Date date, String content) {
        this.date = date;
        this.content = content;
    }
}
