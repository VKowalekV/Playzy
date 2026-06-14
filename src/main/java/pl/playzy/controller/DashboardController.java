package pl.playzy.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.playzy.dto.*;
import pl.playzy.model.User;
import pl.playzy.repository.UserRepository;
import pl.playzy.security.CustomUserDetailsService;
import pl.playzy.service.UserService;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final CustomUserDetailsService customUserDetailsService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails != null) {
            userRepository.findByUsername(userDetails.getUsername().toLowerCase()).ifPresent(user -> {
                addDashboardAttributes(model, user);
            });
        }
        return "dashboard";
    }

    @PostMapping("/dashboard/username")
    public String updateUsername(@Valid @ModelAttribute("usernameUpdateDto") UsernameUpdateDto dto,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(userDetails)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono użytkownika"));

        if (bindingResult.hasErrors()) {
            addDashboardAttributes(model, currentUser);
            return "dashboard";
        }

        try {
            User updatedUser = userService.updateUsername(currentUser, dto.getUsername());
            refreshAuthentication(updatedUser);
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("username", "username.exists", e.getMessage());
            addDashboardAttributes(model, currentUser);
            return "dashboard";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Nazwa użytkownika została zmieniona.");
        return "redirect:/dashboard";
    }

    @PostMapping("/dashboard/email")
    public String updateEmail(@Valid @ModelAttribute("emailUpdateDto") EmailUpdateDto dto,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(userDetails)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono użytkownika"));

        if (bindingResult.hasErrors()) {
            addDashboardAttributes(model, currentUser);
            return "dashboard";
        }

        try {
            userService.updateEmail(currentUser, dto.getEmail());
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("email", "email.exists", e.getMessage());
            addDashboardAttributes(model, currentUser);
            return "dashboard";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Adres e-mail został zmieniony.");
        return "redirect:/dashboard";
    }

    @PostMapping("/dashboard/password")
    public String changePassword(@Valid @ModelAttribute("passwordChangeDto") PasswordChangeDto dto,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(userDetails)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono użytkownika"));

        if (bindingResult.hasErrors()) {
            addDashboardAttributes(model, currentUser);
            return "dashboard";
        }

        try {
            userService.changePassword(currentUser, dto.getCurrentPassword(), dto.getNewPassword());
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("currentPassword", "password.invalid", e.getMessage());
            addDashboardAttributes(model, currentUser);
            return "dashboard";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Hasło zostało zmienione.");
        return "redirect:/dashboard";
    }

    @PostMapping("/dashboard/delete-account")
    public String deleteAccount(@Valid @ModelAttribute("deleteAccountDto") DeleteAccountDto dto,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model,
            HttpServletRequest request) {
        User currentUser = getCurrentUser(userDetails)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono użytkownika"));

        if (bindingResult.hasErrors()) {
            addDashboardAttributes(model, currentUser);
            return "dashboard";
        }

        try {
            userService.deleteOwnAccount(currentUser, dto.getPassword());
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("password", "password.invalid", e.getMessage());
            addDashboardAttributes(model, currentUser);
            return "dashboard";
        }

        SecurityContextHolder.clearContext();
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
        return "redirect:/";
    }

    private java.util.Optional<User> getCurrentUser(UserDetails userDetails) {
        if (userDetails == null) {
            return java.util.Optional.empty();
        }
        return userRepository.findByUsername(userDetails.getUsername().toLowerCase());
    }

    private void addDashboardAttributes(Model model, User user) {
        model.addAttribute("currentUser", UserProfileDto.fromEntity(user));

        if (!model.containsAttribute("usernameUpdateDto")) {
            UsernameUpdateDto usernameUpdateDto = new UsernameUpdateDto();
            usernameUpdateDto.setUsername(user.getUsername());
            model.addAttribute("usernameUpdateDto", usernameUpdateDto);
        }
        if (!model.containsAttribute("emailUpdateDto")) {
            EmailUpdateDto emailUpdateDto = new EmailUpdateDto();
            emailUpdateDto.setEmail(user.getEmail());
            model.addAttribute("emailUpdateDto", emailUpdateDto);
        }
        if (!model.containsAttribute("passwordChangeDto")) {
            model.addAttribute("passwordChangeDto", new PasswordChangeDto());
        }
        if (!model.containsAttribute("deleteAccountDto")) {
            model.addAttribute("deleteAccountDto", new DeleteAccountDto());
        }
    }

    private void refreshAuthentication(User user) {
        UserDetails updatedUserDetails = customUserDetailsService.loadUserByUsername(user.getUsername());
        Authentication currentAuthentication = SecurityContextHolder.getContext().getAuthentication();
        UsernamePasswordAuthenticationToken newAuthentication = new UsernamePasswordAuthenticationToken(
                updatedUserDetails,
                currentAuthentication.getCredentials(),
                updatedUserDetails.getAuthorities());
        newAuthentication.setDetails(currentAuthentication.getDetails());
        SecurityContextHolder.getContext().setAuthentication(newAuthentication);
    }
}
