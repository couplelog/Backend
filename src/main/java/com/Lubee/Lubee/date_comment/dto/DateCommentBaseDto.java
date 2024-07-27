package com.Lubee.Lubee.date_comment.dto;

import com.Lubee.Lubee.date_comment.domain.DateComment;
import com.Lubee.Lubee.enumset.Profile;
import com.Lubee.Lubee.user.domain.User;
import lombok.*;

@Data
@NoArgsConstructor
@ToString
@Getter
public class DateCommentBaseDto {

    private String content;
    private Profile profile;

    public DateCommentBaseDto(String content, Profile profile) {
        this.content = content;
        this.profile = profile;
    }

    public static DateCommentBaseDto of(String content, Profile profile) {
        return new DateCommentBaseDto(content, profile);
    }

    public static DateCommentBaseDto from(DateComment dateComment, User user) {
        if (dateComment == null) {
            return null;
        }
        return new DateCommentBaseDto(dateComment.getContent(), user.getProfile());
    }
}
