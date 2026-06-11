package pl.playzy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.playzy.dto.UserRegistrationDto;
import pl.playzy.model.Role;
import pl.playzy.model.User;
import pl.playzy.repository.UserRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void registerUser(UserRegistrationDto dto) {
        String usernameLower = dto.getUsername().toLowerCase();
        String emailLower = dto.getEmail().toLowerCase();

        if (userRepository.existsByUsername(usernameLower)) {
            throw new IllegalArgumentException("Nazwa użytkownika jest już zajęta");
        }
        if (userRepository.existsByEmail(emailLower)) {
            throw new IllegalArgumentException("Adres e-mail jest już zajęty");
        }

        User user = User.builder()
                .username(usernameLower)
                .email(emailLower)
                .password(passwordEncoder.encode(dto.getPassword()))
                .joinDate(LocalDateTime.now())
                .role(Role.USER)
                .build();

        userRepository.save(user);
    }
}
