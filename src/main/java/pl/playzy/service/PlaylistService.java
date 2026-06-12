package pl.playzy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.playzy.dto.PlaylistCreateDto;
import pl.playzy.model.Playlist;
import pl.playzy.model.User;
import pl.playzy.repository.PlaylistRepository;

import java.time.LocalDateTime;

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
}
