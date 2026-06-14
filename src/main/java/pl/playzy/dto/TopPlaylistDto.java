package pl.playzy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopPlaylistDto {
    private Long id;
    private String name;
    private String description;
    private String ownerUsername;
    private int likesCount;
    private int tracksCount;
}
