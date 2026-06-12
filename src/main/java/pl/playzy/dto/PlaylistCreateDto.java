package pl.playzy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistCreateDto {

    @NotBlank(message = "Nazwa playlisty nie może być pusta")
    @Size(min = 3, max = 100, message = "Nazwa playlisty musi mieć od 3 do 100 znaków")
    private String name;

    @Size(max = 500, message = "Opis może mieć maksymalnie 500 znaków")
    private String description;

    private boolean isPublic;
}
