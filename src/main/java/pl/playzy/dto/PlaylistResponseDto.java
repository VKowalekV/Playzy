package pl.playzy.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonPropertyOrder({
        "id", "name", "description", "public", "ownerUsername",
        "likesCount", "dislikesCount", "followersCount", "totalDurationMinutes"
})
public class PlaylistResponseDto {
    private Long id;
    private String name;
    private String description;
    private boolean isPublic;
    private String ownerUsername;
    private int likesCount;
    private int dislikesCount;
    private int followersCount;
    private double totalDurationMinutes;
}
