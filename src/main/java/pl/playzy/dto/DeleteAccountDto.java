package pl.playzy.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeleteAccountDto {

    @NotBlank(message = "Hasło jest wymagane")
    private String password;
}
