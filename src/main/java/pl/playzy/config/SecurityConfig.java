package pl.playzy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/register", "/login", "/css/**", "/js/**",
                                "/playlists",
                                "/api/playlists/top", "/error")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/playlists/*", "/api/playlists",
                                "/api/playlists/*")
                        .permitAll()
                        .requestMatchers("/admin", "/admin/**").hasRole("ADMIN")
                        .requestMatchers("/moderator", "/moderator/**").hasRole("MODERATOR")
                        .anyRequest().authenticated())
                .exceptionHandling(exception -> exception
                        .defaultAuthenticationEntryPointFor(
                                (request, response, authException) -> {
                                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                                    response.setContentType("application/json;charset=UTF-8");
                                    response.getWriter()
                                            .write("{\"error\": \"Nie jesteś zalogowany.\"}");
                                },
                                new RegexRequestMatcher("^/api/.*", null))
                        .accessDeniedHandler((request, response,
                                accessDeniedException) -> response.sendRedirect("/")))
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll())
                .rememberMe(rememberMe -> rememberMe
                        .key("playzySecretKey")
                        .tokenValiditySeconds(86400 * 7))
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .permitAll());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
