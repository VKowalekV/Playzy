package pl.playzy.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class DateOfBirthUpdateDto {

    @NotNull(message = "Data urodzenia jest wymagana")
    @Past(message = "Data urodzenia musi znajdować się w przeszłości")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;
}
