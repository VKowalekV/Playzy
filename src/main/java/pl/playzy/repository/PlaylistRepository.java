package pl.playzy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.playzy.model.Playlist;

import java.util.List;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    List<Playlist> findByOwnerId(Long ownerId);

    List<Playlist> findByIsPublicTrueAndNameContainingIgnoreCase(String name);

    List<Playlist> findByIsPublicTrueOrderByCreatedAtDesc();

    List<Playlist> findByFollowersId(Long followerId);
}
