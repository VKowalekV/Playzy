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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pl.playzy.dto.PlaylistCreateDto;
import pl.playzy.model.Playlist;
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
    public String libraryMyPlaylists(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        prepareLibraryModel(userDetails, model);
        User owner = (User) model.getAttribute("owner");
        model.addAttribute("activeTab", "my");
        model.addAttribute("myPlaylists", playlistService.getUserPlaylists(owner));
        return "library";
    }

    @GetMapping("/library/followed")
    public String libraryFollowedPlaylists(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        prepareLibraryModel(userDetails, model);
        User owner = (User) model.getAttribute("owner");
        model.addAttribute("activeTab", "followed");
        model.addAttribute("followedPlaylists", playlistService.getFollowedPlaylists(owner));
        return "library";
    }

    @GetMapping("/library/collaborative")
    public String libraryCollaborativePlaylists(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        prepareLibraryModel(userDetails, model);
        User owner = (User) model.getAttribute("owner");
        model.addAttribute("activeTab", "collaborative");
        model.addAttribute("collaborativePlaylists", playlistService.getCoCreatedPlaylists(owner));
        return "library";
    }

    private void prepareLibraryModel(UserDetails userDetails, Model model) {
        if (!model.containsAttribute("playlistCreateDto")) {
            model.addAttribute("playlistCreateDto", new PlaylistCreateDto());
        }
        User owner = userRepository.findByUsername(userDetails.getUsername().toLowerCase())
                .orElseThrow(() -> new RuntimeException("Nie znaleziono użytkownika"));
        model.addAttribute("owner", owner);
    }

    @PostMapping("/playlists/create")
    public String createPlaylist(@Valid @ModelAttribute("playlistCreateDto") PlaylistCreateDto dto,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.playlistCreateDto",
                    bindingResult);
            redirectAttributes.addFlashAttribute("playlistCreateDto", dto);
            redirectAttributes.addFlashAttribute("showCreateModal", true);
            return "redirect:/library";
        }

        User owner = userRepository.findByUsername(userDetails.getUsername().toLowerCase())
                .orElseThrow(() -> new RuntimeException("Nie znaleziono użytkownika"));

        playlistService.createPlaylist(dto, owner);
        return "redirect:/library";
    }

    @GetMapping("/playlists/{id}")
    public String playlistDetails(@PathVariable Long id, Model model,
            @AuthenticationPrincipal UserDetails userDetails) {
        Playlist playlist = playlistService.getPlaylistById(id)
                .orElseThrow(() -> new RuntimeException("Playlist not found"));

        model.addAttribute("playlist", playlist);

        boolean canEdit = false;
        boolean isOwner = false;
        if (userDetails != null) {
            User user = userRepository.findByUsername(userDetails.getUsername().toLowerCase()).orElse(null);
            if (user != null) {
                boolean isAdmin = user.getRole() == pl.playzy.model.Role.ADMIN;
                if (isAdmin || playlist.getOwner().getId().equals(user.getId()) || playlist.getCoCreators().contains(user)) {
                    canEdit = true;
                }
                if (isAdmin || playlist.getOwner().getId().equals(user.getId())) {
                    isOwner = true;
                }
            }
        }
        model.addAttribute("canEdit", canEdit);
        model.addAttribute("isOwner", isOwner);

        if (!model.containsAttribute("playlistUpdateDto")) {
            PlaylistCreateDto dto = new PlaylistCreateDto();
            dto.setName(playlist.getName());
            dto.setDescription(playlist.getDescription());
            dto.setPublic(playlist.isPublic());
            model.addAttribute("playlistUpdateDto", dto);
        }

        return "playlist-details";
    }

    @PostMapping("/playlists/{id}/edit")
    public String editPlaylist(@PathVariable Long id, @Valid @ModelAttribute("playlistUpdateDto") PlaylistCreateDto dto,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.playlistUpdateDto",
                    bindingResult);
            redirectAttributes.addFlashAttribute("playlistUpdateDto", dto);
            redirectAttributes.addFlashAttribute("showEditModal", true);
            return "redirect:/playlists/" + id;
        }

        User currentUser = userRepository.findByUsername(userDetails.getUsername().toLowerCase())
                .orElseThrow(() -> new RuntimeException("Nie znaleziono użytkownika"));

        playlistService.updatePlaylist(id, dto, currentUser);
        return "redirect:/playlists/" + id;
    }

    @ModelAttribute("currentUser")
    public User populateCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            return userRepository.findByUsername(userDetails.getUsername().toLowerCase()).orElse(null);
        }
        return null;
    }

    @PostMapping("/playlists/{id}/co-creators/add")
    public String addCoCreator(@PathVariable Long id, @RequestParam("username") String username,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        if (userDetails != null) {
            User currentUser = userRepository.findByUsername(userDetails.getUsername().toLowerCase()).orElse(null);
            if (currentUser != null) {
                try {
                    playlistService.addCoCreator(id, username, currentUser);
                    redirectAttributes.addFlashAttribute("coCreatorSuccess", "Pomyślnie dodano współtwórcę!");
                } catch (Exception e) {
                    redirectAttributes.addFlashAttribute("coCreatorError", e.getMessage());
                    redirectAttributes.addFlashAttribute("showAddCoCreatorModal", true);
                }
            }
        }
        return "redirect:/playlists/" + id;
    }

    @PostMapping("/playlists/{id}/delete")
    public String deletePlaylist(@PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            User user = userRepository.findByUsername(userDetails.getUsername().toLowerCase()).orElse(null);
            if (user != null) {
                playlistService.deletePlaylist(id, user);
            }
        }
        return "redirect:/library";
    }

    @PostMapping("/playlists/{id}/tracks/{trackId}/delete")
    public String deleteTrack(@PathVariable Long id, @PathVariable Long trackId,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            User user = userRepository.findByUsername(userDetails.getUsername().toLowerCase()).orElse(null);
            if (user != null) {
                playlistService.removeTrack(id, trackId, user);
            }
        }
        return "redirect:/playlists/" + id;
    }
}
