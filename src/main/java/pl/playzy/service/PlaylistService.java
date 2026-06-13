package pl.playzy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.playzy.dto.PlaylistCreateDto;
import pl.playzy.model.Playlist;
import pl.playzy.model.User;
import pl.playzy.repository.PlaylistRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaylistService {

    private final PlaylistRepository playlistRepository;

    @Transactional
    public void createPlaylist(PlaylistCreateDto dto, User owner) {
        Playlist playlist = Playlist.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .isPublic(dto.isPublic())
                .owner(owner)
                .createdAt(LocalDateTime.now())
                .build();
                
        playlistRepository.save(playlist);
    }

    public List<Playlist> getUserPlaylists(User user) {
        return playlistRepository.findByOwnerId(user.getId());
    }

    public List<Playlist> getFollowedPlaylists(User user) {
        return playlistRepository.findByFollowersId(user.getId());
    }

    public List<Playlist> getPublicPlaylists() {
        return playlistRepository.findByIsPublicTrueOrderByCreatedAtDesc();
    }
}
