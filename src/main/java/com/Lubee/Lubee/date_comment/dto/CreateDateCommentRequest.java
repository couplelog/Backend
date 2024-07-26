package com.Lubee.Lubee.date_comment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Getter
@NoArgsConstructor
public class CreateDateCommentRequest {

    private String content;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd")
    private Date date;

    public CreateDateCommentRequest(String content, Date date) {
        this.content = content;
        this.date = date;
    }
}
