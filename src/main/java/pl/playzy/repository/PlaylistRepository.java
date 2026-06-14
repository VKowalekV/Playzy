package pl.playzy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import pl.playzy.model.Playlist;

import java.util.List;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    List<Playlist> findByOwnerId(Long ownerId);

    List<Playlist> findAllByOrderByCreatedAtDesc();

    List<Playlist> findByNameContainingIgnoreCaseOrderByCreatedAtDesc(String name);

    List<Playlist> findByIsPublicTrueAndNameContainingIgnoreCase(String name);

    List<Playlist> findByIsPublicTrueOrderByCreatedAtDesc();

    List<Playlist> findByFollowersId(Long followerId);

    List<Playlist> findByCoCreatorsId(Long coCreatorId);

    @Query(value = "SELECT p.* FROM playlists p " +
            "LEFT JOIN playlist_ratings r ON p.id = r.playlist_id AND r.is_like = true " +
            "WHERE p.is_public = true " +
            "GROUP BY p.id " +
            "ORDER BY COUNT(r.id) DESC " +
            "LIMIT 1", nativeQuery = true)
    Playlist findMostLikedPublicPlaylist();
}
