package pl.playzy.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pl.playzy.model.User;
import pl.playzy.repository.UserRepository;
import pl.playzy.service.PlaylistService;

@Controller
@RequestMapping("/moderator")
@RequiredArgsConstructor
public class ModeratorController {

    private final PlaylistService playlistService;
    private final UserRepository userRepository;

    @GetMapping
    public String moderatorPanel() {
        return "redirect:/moderator/playlists";
    }

    @GetMapping("/playlists")
    public String playlists(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("allPlaylists", playlistService.searchPlaylists(q));
        model.addAttribute("searchQuery", q);
        model.addAttribute("panelTitle", "Panel Moderatora");
        model.addAttribute("panelHomePath", "/moderator/playlists");
        model.addAttribute("playlistsPath", "/moderator/playlists");
        model.addAttribute("playlistDeletePathPrefix", "/moderator/playlists/");
        model.addAttribute("showUsersTab", false);
        return "staff-playlists";
    }

    @PostMapping("/playlists/{id}/delete")
    public String deletePlaylist(@PathVariable Long id) {
        playlistService.deletePlaylistAsStaff(id);
        return "redirect:/moderator/playlists";
    }

    @ModelAttribute("currentUser")
    public User populateCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            return userRepository.findByUsername(userDetails.getUsername().toLowerCase()).orElse(null);
        }
        return null;
    }
}
