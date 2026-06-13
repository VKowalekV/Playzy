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

    @Transactional
    public void deletePlaylist(Long id, User currentUser) {
        playlistRepository.findById(id).ifPresent(playlist -> {
            if (playlist.getOwner().getId().equals(currentUser.getId())) {
                playlistRepository.delete(playlist);
            }
        });
    }

    @Transactional
    public Playlist toggleFollow(Long id, User currentUser) {
        return playlistRepository.findById(id).map(playlist -> {
            if (playlist.isFollowedBy(currentUser)) {
                playlist.getFollowers().removeIf(u -> u.getId().equals(currentUser.getId()));
            } else {
                playlist.getFollowers().add(currentUser);
            }
            return playlistRepository.save(playlist);
        }).orElse(null);
    }

    @Transactional
    public Playlist ratePlaylist(Long id, User currentUser, boolean isLike) {
        return playlistRepository.findById(id).map(playlist -> {
            Boolean currentRating = playlist.getUserRating(currentUser);
            if (currentRating != null) {
                if (currentRating == isLike) {
                    playlist.getRatings().removeIf(r -> r.getUser().getId().equals(currentUser.getId()));
                } else {
                    playlist.getRatings().stream()
                            .filter(r -> r.getUser().getId().equals(currentUser.getId()))
                            .findFirst()
                            .ifPresent(r -> r.setLike(isLike));
                }
            } else {
                pl.playzy.model.PlaylistRating newRating = pl.playzy.model.PlaylistRating.builder()
                        .user(currentUser)
                        .playlist(playlist)
                        .isLike(isLike)
                        .build();
                playlist.getRatings().add(newRating);
            }
            return playlistRepository.save(playlist);
        }).orElse(null);
    }
}
