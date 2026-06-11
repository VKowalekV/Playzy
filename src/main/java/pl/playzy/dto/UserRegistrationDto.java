package pl.playzy.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegistrationDto {

    @NotBlank(message = "Nazwa użytkownika jest wymagana")
    @Size(min = 3, max = 50, message = "Nazwa użytkownika musi mieć od 3 do 50 znaków")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Nazwa użytkownika może zawierać tylko litery, cyfry, podkreślenia i myślniki")
    private String username;

    @NotBlank(message = "Email jest wymagany")
    @Email(message = "Nieprawidłowy format email")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,20}$", message = "Adres email musi posiadać poprawną domenę (np. .com, .pl)")
    private String email;

    @NotBlank(message = "Hasło jest wymagane")
    @Size(min = 8, message = "Hasło musi mieć co najmniej 8 znaków")
    @Pattern(regexp = ".*[a-z].*", message = "Hasło musi zawierać co najmniej jedną małą literę")
    @Pattern(regexp = ".*[A-Z].*", message = "Hasło musi zawierać co najmniej jedną dużą literę")
    @Pattern(regexp = ".*\\d.*", message = "Hasło musi zawierać co najmniej jedną cyfrę")
    @Pattern(regexp = ".*[@$!%*?&_\\-+#].*", message = "Hasło musi zawierać co najmniej jeden znak specjalny")
    @Pattern(regexp = "^[^\\s]+$", message = "Hasło nie może zawierać spacji")
    private String password;

    @NotBlank(message = "Powtórz hasło")
    private String confirmPassword;

    @AssertTrue(message = "Hasła nie są identyczne")
    public boolean isPasswordsEqual() {
        if (password == null || confirmPassword == null) {
            return true;
        }
        return password.equals(confirmPassword);
    }
}
