package pl.playzy.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pl.playzy.model.Playlist;
import pl.playzy.model.User;
import pl.playzy.repository.UserRepository;
import pl.playzy.service.PlaylistService;
import pl.playzy.dto.TrackDto;
import pl.playzy.dto.TopPlaylistDto;
import pl.playzy.dto.PlaylistCreateDto;
import pl.playzy.dto.PlaylistResponseDto;
import pl.playzy.model.PlaylistTrack;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/playlists")
@RequiredArgsConstructor
public class PlaylistRestController {

    private final PlaylistService playlistService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<PlaylistResponseDto>> getAllPlaylists() {
        List<PlaylistResponseDto> playlists = playlistService.getPublicPlaylists().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(playlists);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlaylistResponseDto> getPlaylist(@PathVariable Long id) {
        return playlistService.getPlaylistById(id)
                .map(playlist -> ResponseEntity.ok(mapToDto(playlist)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createPlaylist(
            @Valid @RequestBody PlaylistCreateDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        User user = userRepository.findByUsername(userDetails.getUsername().toLowerCase()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            playlistService.createPlaylist(dto, user);
            return ResponseEntity.status(201).body(Map.of("message", "Playlista została pomyślnie utworzona"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePlaylist(
            @PathVariable Long id,
            @Valid @RequestBody PlaylistCreateDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        User user = userRepository.findByUsername(userDetails.getUsername().toLowerCase()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            playlistService.updatePlaylist(id, dto, user);
            return ResponseEntity.ok(Map.of("message", "Playlista została zaktualizowana"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePlaylist(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        User user = userRepository.findByUsername(userDetails.getUsername().toLowerCase()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            playlistService.deletePlaylist(id, user);
            return ResponseEntity.ok(Map.of("message", "Playlista została usunięta"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/top")
    public ResponseEntity<TopPlaylistDto> getTopPlaylist() {
        Playlist top = playlistService.getMostLikedPublicPlaylist();
        if (top == null) {
            return ResponseEntity.notFound().build();
        }
        TopPlaylistDto dto = TopPlaylistDto.builder()
                .id(top.getId())
                .name(top.getName())
                .description(top.getDescription())
                .ownerUsername(top.getOwner().getUsername())
                .likesCount(top.getLikesCount())
                .tracksCount(top.getTracks().size())
                .build();
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{id}/toggle-follow")
    public ResponseEntity<Map<String, Object>> toggleFollow(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        User user = userRepository.findByUsername(userDetails.getUsername().toLowerCase()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        Playlist updatedPlaylist = playlistService.toggleFollow(id, user);
        if (updatedPlaylist == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("followed", updatedPlaylist.isFollowedBy(user));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/rate")
    public ResponseEntity<Map<String, Object>> ratePlaylist(
            @PathVariable Long id,
            @RequestParam boolean like,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        User user = userRepository.findByUsername(userDetails.getUsername().toLowerCase()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        Playlist updatedPlaylist = playlistService.ratePlaylist(id, user, like);
        if (updatedPlaylist == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("likesCount", updatedPlaylist.getLikesCount());
        response.put("dislikesCount", updatedPlaylist.getDislikesCount());
        response.put("userRating", updatedPlaylist.getUserRating(user));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/tracks")
    public ResponseEntity<?> addTrack(
            @PathVariable Long id,
            @RequestBody TrackDto trackDto,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        User user = userRepository.findByUsername(userDetails.getUsername().toLowerCase()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            PlaylistTrack addedTrack = playlistService.addTrack(id, user, trackDto);
            return ResponseEntity.ok(addedTrack);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private PlaylistResponseDto mapToDto(Playlist playlist) {
        return PlaylistResponseDto.builder()
                .id(playlist.getId())
                .name(playlist.getName())
                .description(playlist.getDescription())
                .isPublic(playlist.isPublic())
                .ownerUsername(playlist.getOwner().getUsername())
                .likesCount(playlist.getLikesCount())
                .dislikesCount(playlist.getDislikesCount())
                .followersCount(playlist.getFollowersCount())
                .totalDurationMinutes(playlist.getTotalDurationMinutes())
                .build();
    }
}
