package pl.playzy.service;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.playzy.dto.PlaylistCreateDto;
import pl.playzy.dto.TrackDto;
import pl.playzy.model.Playlist;
import pl.playzy.model.PlaylistRating;
import pl.playzy.model.PlaylistTrack;
import pl.playzy.model.User;
import pl.playzy.repository.PlaylistRepository;
import pl.playzy.repository.PlaylistTrackRepository;
import pl.playzy.repository.UserRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistTrackRepository playlistTrackRepository;
    private final UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

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

    @Transactional
    public void updatePlaylist(Long id, PlaylistCreateDto dto, User currentUser) {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono playlisty"));

        boolean isAdmin = currentUser.getRole() == pl.playzy.model.Role.ADMIN;
        if (!isAdmin && !playlist.getOwner().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Tylko właściciel może edytować ustawienia playlisty");
        }

        playlist.setName(dto.getName());
        playlist.setDescription(dto.getDescription());
        playlist.setPublic(dto.isPublic());

        playlistRepository.save(playlist);
    }

    @Transactional
    public void addCoCreator(Long playlistId, String username, User currentUser) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono playlisty"));

        boolean isAdmin = currentUser.getRole() == pl.playzy.model.Role.ADMIN;
        if (!isAdmin && !playlist.getOwner().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Tylko właściciel może dodawać współtwórców");
        }

        User newCoCreator = userRepository.findByUsername(username.toLowerCase())
                .orElseThrow(() -> new RuntimeException("Nie znaleziono użytkownika o podanej nazwie: " + username));

        if (newCoCreator.getId().equals(playlist.getOwner().getId())) {
            throw new RuntimeException("Właściciel nie może być współtwórcą");
        }

        if (playlist.getCoCreators().contains(newCoCreator)) {
            throw new RuntimeException("Ten użytkownik jest już współtwórcą");
        }

        playlist.getCoCreators().add(newCoCreator);
        playlistRepository.save(playlist);
    }

    public List<Playlist> getUserPlaylists(User user) {
        return playlistRepository.findByOwnerId(user.getId());
    }

    public List<Playlist> getFollowedPlaylists(User user) {
        return playlistRepository.findByFollowersId(user.getId());
    }

    public List<Playlist> getCoCreatedPlaylists(User user) {
        return playlistRepository.findByCoCreatorsId(user.getId());
    }

    public List<Playlist> getPublicPlaylists() {
        return playlistRepository.findByIsPublicTrueOrderByCreatedAtDesc();
    }

    public List<Playlist> getPublicPlaylistsFiltered(String sortField, String sortDir, String dateFilter,
            String ownerUsername) {
        String finalSortField = "createdAt";
        if ("likesCount".equals(sortField) || "dislikesCount".equals(sortField) || "followersCount".equals(sortField)) {
            finalSortField = sortField;
        }

        Sort sort = Sort.by(
                "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC
                        : Sort.Direction.DESC,
                finalSortField);

        LocalDateTime dateFrom = null;
        if (dateFilter != null) {
            switch (dateFilter) {
                case "day":
                    dateFrom = LocalDateTime.now().minusDays(1);
                    break;
                case "week":
                    dateFrom = LocalDateTime.now().minusWeeks(1);
                    break;
                case "month":
                    dateFrom = LocalDateTime.now().minusMonths(1);
                    break;
            }
        }

        boolean hasDateFilter = dateFrom != null;
        boolean hasOwnerFilter = ownerUsername != null && !ownerUsername.trim().isEmpty();

        return playlistRepository.findPublicPlaylistsWithFilters(hasDateFilter, dateFrom, hasOwnerFilter, ownerUsername,
                sort);
    }

    public List<Playlist> getAllPlaylists() {
        return playlistRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Playlist> searchPlaylists(String query) {
        if (query == null || query.isBlank()) {
            return getAllPlaylists();
        }
        return playlistRepository.findByNameContainingIgnoreCaseOrderByCreatedAtDesc(query.trim());
    }

    public Playlist getMostLikedPublicPlaylist() {
        return playlistRepository.findFirstByIsPublicTrueOrderByLikesCountDesc();
    }

    @Transactional
    public void deletePlaylist(Long id, User currentUser) {
        playlistRepository.findById(id).ifPresent(playlist -> {
            boolean isAdmin = currentUser.getRole() == pl.playzy.model.Role.ADMIN;
            if (isAdmin || playlist.getOwner().getId().equals(currentUser.getId())) {
                playlistRepository.delete(playlist);
            }
        });
    }

    @Transactional
    public void deletePlaylistAsAdmin(Long id) {
        playlistRepository.findById(id).ifPresent(playlistRepository::delete);
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
                PlaylistRating newRating = PlaylistRating.builder()
                        .user(currentUser)
                        .playlist(playlist)
                        .isLike(isLike)
                        .build();
                playlist.getRatings().add(newRating);
            }
            playlist = playlistRepository.save(playlist);
            entityManager.flush();
            entityManager.refresh(playlist);
            return playlist;
        }).orElse(null);
    }

    public Optional<Playlist> getPlaylistById(Long id) {
        return playlistRepository.findById(id);
    }

    @Transactional
    public PlaylistTrack addTrack(Long playlistId, User currentUser, TrackDto trackDto) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("Nie znalexiono playlisty"));

        boolean isAdmin = currentUser.getRole() == pl.playzy.model.Role.ADMIN;
        if (!isAdmin && !playlist.getOwner().getId().equals(currentUser.getId()) &&
                !playlist.getCoCreators().contains(currentUser)) {
            throw new RuntimeException("Nie masz uprawnień by edytować tę playlistę");
        }

        boolean alreadyExists = playlist.getTracks().stream()
                .anyMatch(t -> t.getSpotifyId().equals(trackDto.getId()));

        if (alreadyExists) {
            throw new RuntimeException("Ten utwór znajduje się już na playliście");
        }

        PlaylistTrack track = PlaylistTrack.builder()
                .spotifyId(trackDto.getId())
                .title(trackDto.getTitle())
                .artist(trackDto.getArtist())
                .durationMinutes(trackDto.getDurationMinutes())
                .addedAt(LocalDateTime.now())
                .playlist(playlist)
                .build();

        track = playlistTrackRepository.save(track);
        playlist.getTracks().add(track);

        return track;
    }

    @Transactional
    public void removeTrack(Long playlistId, Long trackId, User currentUser) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono playlisty"));

        boolean isAdmin = currentUser.getRole() == pl.playzy.model.Role.ADMIN;
        if (!isAdmin && !playlist.getOwner().getId().equals(currentUser.getId()) &&
                !playlist.getCoCreators().contains(currentUser)) {
            throw new RuntimeException("Nie masz uprawnień by edytować tę playlistę");
        }

        playlist.getTracks().removeIf(track -> track.getId().equals(trackId));
        playlistRepository.save(playlist);
    }
}
