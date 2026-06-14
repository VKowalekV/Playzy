package pl.playzy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackDto {
    private String id; // jak coś to to jest spotify id a nie id z naszej bazy
    private String title;
    private String artist;
    private String albumImageUrl;
    private double durationMinutes;
    private String previewUrl;
}
