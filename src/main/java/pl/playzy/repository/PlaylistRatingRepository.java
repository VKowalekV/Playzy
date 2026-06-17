package pl.playzy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.playzy.model.PlaylistRating;

import java.util.Optional;

public interface PlaylistRatingRepository extends JpaRepository<PlaylistRating, Long> {
    void deleteByUserId(Long userId);
    Optional<PlaylistRating> findByPlaylistIdAndUserId(Long playlistId, Long userId);
}
