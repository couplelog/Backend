package com.Lubee.Lubee.memory.dto;

import com.Lubee.Lubee.enumset.Profile;
import com.Lubee.Lubee.enumset.Reaction;
import lombok.*;

@Data
@NoArgsConstructor
@ToString
@Getter
public class MemoryBaseDto {

    private Long memory_id;
    //private Long user_id;
    private String location_name;
    private String picture;
    private Profile writer_profile_first;
    private Profile writer_profile_second;
    private Reaction reaction_first;
    private Reaction reaction_second;
    private String upload_time;

    public MemoryBaseDto(Long memory_id, String location_name, String picture, Profile writer_profile_first, Profile writer_profile_second, Reaction reaction_first, Reaction reaction_second, String upload_time)
    {
        this.memory_id = memory_id;
        //this.user_id =user_id;
        this.location_name = location_name;
        this.picture = picture;
        this.writer_profile_first = writer_profile_first;
        this.writer_profile_second = writer_profile_second;
        this.reaction_first = reaction_first;
        this.reaction_second = reaction_second;
        this.upload_time = upload_time;
    }
    public static MemoryBaseDto of(Long memory_id, String location_name, String picture, Profile writer_profile_first, Profile writer_profile_second, Reaction reaction1, Reaction reaction2, String upload_time)
    {
        return new MemoryBaseDto(memory_id, location_name, picture, writer_profile_first, writer_profile_second, reaction1, reaction2, upload_time);
    }

}
