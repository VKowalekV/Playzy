package pl.playzy.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pl.playzy.model.User;
import pl.playzy.repository.UserRepository;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        User user;
        String loginLower = login.toLowerCase();

        if (loginLower.contains("@")) {
            user = userRepository.findByEmail(loginLower)
                    .orElseThrow(() -> new UsernameNotFoundException("Nie znaleziono emaila: " + login));
        } else {
            user = userRepository.findByUsername(loginLower)
                    .orElseThrow(() -> new UsernameNotFoundException("Nie znaleziono użytkownika: " + login));
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
    }
}
