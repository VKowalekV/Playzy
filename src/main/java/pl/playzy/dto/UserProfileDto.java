package pl.playzy.dto;

import lombok.Builder;
import lombok.Data;
import pl.playzy.model.User;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class UserProfileDto {
    private String username;
    private String email;
    private LocalDate dateOfBirth;
    private LocalDateTime joinDate;

    public static UserProfileDto fromEntity(User user) {
        return UserProfileDto.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .dateOfBirth(user.getDateOfBirth())
                .joinDate(user.getJoinDate())
                .build();
    }
}
