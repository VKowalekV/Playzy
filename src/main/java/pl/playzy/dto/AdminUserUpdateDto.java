package pl.playzy.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Data
public class AdminUserUpdateDto {

    @NotBlank(message = "Nazwa użytkownika jest wymagana")
    @Size(min = 3, max = 50, message = "Nazwa użytkownika musi mieć od 3 do 50 znaków")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Nazwa użytkownika może zawierać tylko litery, cyfry, podkreślenia i myślniki")
    private String username;

    @NotBlank(message = "Email jest wymagany")
    @Email(message = "Nieprawidłowy format email")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,20}$", message = "Adres email musi posiadać poprawną domenę (np. .com, .pl)")
    private String email;

    @NotNull(message = "Data urodzenia jest wymagana")
    @Past(message = "Data urodzenia musi znajdować się w przeszłości")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    private boolean moderator;
}
