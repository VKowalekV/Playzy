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
import org.springframework.web.bind.annotation.PostMapping;
import pl.playzy.dto.PlaylistCreateDto;
import pl.playzy.model.User;
import pl.playzy.repository.UserRepository;
import pl.playzy.service.PlaylistService;

@Controller
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService;
    private final UserRepository userRepository;

    @GetMapping("/playlists")
    public String publicPlaylists(Model model) {
        model.addAttribute("publicPlaylists", playlistService.getPublicPlaylists());
        return "playlists";
    }

    @GetMapping("/library")
    public String library(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (!model.containsAttribute("playlistCreateDto")) {
            model.addAttribute("playlistCreateDto", new PlaylistCreateDto());
        }
        
        User owner = userRepository.findByUsername(userDetails.getUsername().toLowerCase())
                .orElseThrow(() -> new RuntimeException("Nie znaleziono użytkownika"));

        model.addAttribute("myPlaylists", playlistService.getUserPlaylists(owner));
        model.addAttribute("followedPlaylists", playlistService.getFollowedPlaylists(owner));

        return "library";
    }

    @PostMapping("/playlists/create")
    public String createPlaylist(@Valid @ModelAttribute("playlistCreateDto") PlaylistCreateDto dto,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("showCreateModal", true);
            return "library";
        }

        User owner = userRepository.findByUsername(userDetails.getUsername().toLowerCase())
                .orElseThrow(() -> new RuntimeException("Nie znaleziono użytkownika"));

        playlistService.createPlaylist(dto, owner);
        return "redirect:/library";
    }
}
