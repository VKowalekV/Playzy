package pl.playzy.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordChangeDto {

    @NotBlank(message = "Obecne hasło jest wymagane")
    private String currentPassword;

    @NotBlank(message = "Nowe hasło jest wymagane")
    @Size(min = 8, message = "Hasło musi mieć co najmniej 8 znaków")
    @Pattern(regexp = ".*[a-z].*", message = "Hasło musi zawierać co najmniej jedną małą literę")
    @Pattern(regexp = ".*[A-Z].*", message = "Hasło musi zawierać co najmniej jedną dużą literę")
    @Pattern(regexp = ".*\\d.*", message = "Hasło musi zawierać co najmniej jedną cyfrę")
    @Pattern(regexp = ".*[@$!%*?&_\\-+#].*", message = "Hasło musi zawierać co najmniej jeden znak specjalny")
    @Pattern(regexp = "^[^\\s]+$", message = "Hasło nie może zawierać spacji")
    private String newPassword;

    @NotBlank(message = "Powtórz nowe hasło")
    private String confirmPassword;

    @AssertTrue(message = "Hasła nie są identyczne")
    public boolean isPasswordsEqual() {
        if (newPassword == null || confirmPassword == null) {
            return true;
        }
        return newPassword.equals(confirmPassword);
    }
}
