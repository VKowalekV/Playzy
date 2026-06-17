package pl.playzy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.playzy.model.PlaylistRating;

public interface PlaylistRatingRepository extends JpaRepository<PlaylistRating, Long> {
    void deleteByUserId(Long userId);
}
