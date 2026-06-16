package pl.playzy.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.playzy.dto.AdminUserUpdateDto;
import pl.playzy.model.User;
import pl.playzy.repository.UserRepository;
import pl.playzy.service.PlaylistService;
import pl.playzy.service.UserService;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final PlaylistService playlistService;
    private final UserRepository userRepository;

    @GetMapping
    public String adminPanel() {
        return "redirect:/admin/users";
    }

    @GetMapping("/users")
    public String users(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("users", userService.searchUsers(q));
        model.addAttribute("searchQuery", q);
        return "admin-users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        User currentAdmin = getCurrentUser(userDetails);
        boolean deleted = userService.deleteUser(id, currentAdmin);
        if (!deleted) {
            redirectAttributes.addFlashAttribute("errorMessage", "Nie można usunąć tego użytkownika.");
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/update")
    public String updateUser(@PathVariable Long id,
            @Valid @ModelAttribute AdminUserUpdateDto dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addEditUserModalError(redirectAttributes, id, dto, bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/admin/users";
        }

        boolean updated;
        try {
            updated = userService.updateUserAsAdmin(id, dto);
        } catch (IllegalArgumentException e) {
            addEditUserModalError(redirectAttributes, id, dto, e.getMessage());
            return "redirect:/admin/users";
        }

        if (!updated) {
            addEditUserModalError(redirectAttributes, id, dto, "Nie można edytować tego użytkownika.");
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/playlists")
    public String playlists(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("allPlaylists", playlistService.searchPlaylists(q));
        model.addAttribute("searchQuery", q);
        model.addAttribute("panelTitle", "Panel Admina");
        model.addAttribute("panelHomePath", "/admin/users");
        model.addAttribute("usersPath", "/admin/users");
        model.addAttribute("playlistsPath", "/admin/playlists");
        model.addAttribute("playlistDeletePathPrefix", "/admin/playlists/");
        model.addAttribute("showUsersTab", true);
        return "staff-playlists";
    }

    @PostMapping("/playlists/{id}/delete")
    public String deletePlaylist(@PathVariable Long id) {
        playlistService.deletePlaylistAsStaff(id);
        return "redirect:/admin/playlists";
    }

    @ModelAttribute("currentUser")
    public User populateCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        return getCurrentUser(userDetails);
    }

    private User getCurrentUser(UserDetails userDetails) {
        if (userDetails != null) {
            return userRepository.findByUsername(userDetails.getUsername().toLowerCase()).orElse(null);
        }
        return null;
    }

    private void addEditUserModalError(RedirectAttributes redirectAttributes, Long id, AdminUserUpdateDto dto,
            String message) {
        redirectAttributes.addFlashAttribute("editUserModalOpen", true);
        redirectAttributes.addFlashAttribute("editUserError", message);
        redirectAttributes.addFlashAttribute("editUserId", id);
        redirectAttributes.addFlashAttribute("editUsername", dto.getUsername());
        redirectAttributes.addFlashAttribute("editEmail", dto.getEmail());
        redirectAttributes.addFlashAttribute("editModerator", dto.isModerator());
    }
}
