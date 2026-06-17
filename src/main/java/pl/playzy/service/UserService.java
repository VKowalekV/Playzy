package pl.playzy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.playzy.dto.AdminUserUpdateDto;
import pl.playzy.dto.UserRegistrationDto;
import pl.playzy.model.Role;
import pl.playzy.model.User;
import pl.playzy.repository.PlaylistRatingRepository;
import pl.playzy.repository.PlaylistRepository;
import pl.playzy.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PlaylistRepository playlistRepository;
    private final PlaylistRatingRepository playlistRatingRepository;
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
                .dateOfBirth(dto.getDateOfBirth())
                .role(Role.USER)
                .build();

        userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAllByOrderByJoinDateDesc();
    }

    public List<User> searchUsers(String query) {
        if (query == null || query.isBlank()) {
            return getAllUsers();
        }
        return userRepository.findByUsernameContainingIgnoreCaseOrderByJoinDateDesc(query.trim());
    }

    @Transactional
    public User updateUsername(User currentUser, String username) {
        String usernameLower = username.trim().toLowerCase();
        if (userRepository.existsByUsernameIgnoreCaseAndIdNot(usernameLower, currentUser.getId())) {
            throw new IllegalArgumentException("Nazwa użytkownika jest już zajęta");
        }

        currentUser.setUsername(usernameLower);
        return userRepository.save(currentUser);
    }

    @Transactional
    public void updateEmail(User currentUser, String email) {
        String emailLower = email.trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCaseAndIdNot(emailLower, currentUser.getId())) {
            throw new IllegalArgumentException("Adres e-mail jest już zajęty");
        }

        currentUser.setEmail(emailLower);
        userRepository.save(currentUser);
    }

    @Transactional
    public void updateDateOfBirth(User currentUser, LocalDate dateOfBirth) {
        currentUser.setDateOfBirth(dateOfBirth);
        userRepository.save(currentUser);
    }

    @Transactional
    public boolean updateUserAsAdmin(Long userId, AdminUserUpdateDto dto) {
        return userRepository.findById(userId).map(user -> {
            if (user.getRole() == Role.ADMIN) {
                return false;
            }

            String usernameLower = dto.getUsername().trim().toLowerCase();
            String emailLower = dto.getEmail().trim().toLowerCase();

            if (userRepository.existsByUsernameIgnoreCaseAndIdNot(usernameLower, user.getId())) {
                throw new IllegalArgumentException("Nazwa użytkownika jest już zajęta");
            }
            if (userRepository.existsByEmailIgnoreCaseAndIdNot(emailLower, user.getId())) {
                throw new IllegalArgumentException("Adres e-mail jest już zajęty");
            }

            user.setUsername(usernameLower);
            user.setEmail(emailLower);
            user.setDateOfBirth(dto.getDateOfBirth());
            user.setRole(dto.isModerator() ? Role.MODERATOR : Role.USER);
            userRepository.save(user);
            return true;
        }).orElse(false);
    }

    @Transactional
    public void changePassword(User currentUser, String currentPassword, String newPassword) {
        if (!passwordEncoder.matches(currentPassword, currentUser.getPassword())) {
            throw new IllegalArgumentException("Obecne hasło jest nieprawidłowe");
        }

        currentUser.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(currentUser);
    }

    @Transactional
    public void deleteOwnAccount(User currentUser, String password) {
        if (!passwordEncoder.matches(password, currentUser.getPassword())) {
            throw new IllegalArgumentException("Hasło jest nieprawidłowe");
        }

        removeUserRelations(currentUser.getId());
        userRepository.delete(currentUser);
    }

    @Transactional
    public boolean deleteUser(Long userId, User currentAdmin) {
        if (currentAdmin != null && currentAdmin.getId().equals(userId)) {
            return false;
        }

        return userRepository.findById(userId).map(user -> {
            removeUserRelations(userId);
            userRepository.delete(user);
            return true;
        }).orElse(false);
    }

    private void removeUserRelations(Long userId) {
        playlistRepository.findByFollowersId(userId).forEach(playlist -> {
            playlist.getFollowers().removeIf(follower -> follower.getId().equals(userId));
            playlistRepository.save(playlist);
        });

        playlistRepository.findByCoCreatorsId(userId).forEach(playlist -> {
            playlist.getCoCreators().removeIf(coCreator -> coCreator.getId().equals(userId));
            playlistRepository.save(playlist);
        });

        playlistRatingRepository.deleteByUserId(userId);
    }
}
