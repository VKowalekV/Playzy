package pl.playzy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.playzy.model.Playlist;
import pl.playzy.model.PlaylistRating;
import pl.playzy.model.User;

import java.util.Optional;

public interface PlaylistRatingRepository extends JpaRepository<PlaylistRating, Long> {
    Optional<PlaylistRating> findByUserAndPlaylist(User user, Playlist playlist);
    long countByPlaylistAndIsLikeTrue(Playlist playlist);
    long countByPlaylistAndIsLikeFalse(Playlist playlist);
}
