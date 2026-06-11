package pl.playzy.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import pl.playzy.dto.UserRegistrationDto;
import pl.playzy.service.UserService;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new UserRegistrationDto());
        return "register";
    }

    @PostMapping("/register")
    public String registerSubmit(@Valid @ModelAttribute("user") UserRegistrationDto userDto, BindingResult result,
            Model model) {
        if (result.hasErrors()) {
            return "register";
        }

        try {
            userService.registerUser(userDto);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "register";
        }

        return "redirect:/login?registered";
    }
}
