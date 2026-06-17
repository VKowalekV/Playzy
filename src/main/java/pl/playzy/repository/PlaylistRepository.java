package pl.playzy.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pl.playzy.model.Playlist;

import java.util.List;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    List<Playlist> findByOwnerId(Long ownerId);

    List<Playlist> findAllByOrderByCreatedAtDesc();

    List<Playlist> findByNameContainingIgnoreCaseOrderByCreatedAtDesc(String name);
    
    List<Playlist> findByIsPublicTrueOrderByCreatedAtDesc();

    List<Playlist> findByFollowersId(Long followerId);

    List<Playlist> findByCoCreatorsId(Long coCreatorId);

    Playlist findFirstByIsPublicTrueOrderByLikesCountDesc();

    boolean existsByIdAndFollowersId(Long playlistId, Long followerId);

    @Modifying
    @Query(value = "INSERT INTO playlist_followers (playlist_id, user_id) VALUES (:playlistId, :userId)", nativeQuery = true)
    void addFollower(@Param("playlistId") Long playlistId, @Param("userId") Long userId);

    @Modifying
    @Query(value = "DELETE FROM playlist_followers WHERE playlist_id = :playlistId AND user_id = :userId", nativeQuery = true)
    void removeFollower(@Param("playlistId") Long playlistId, @Param("userId") Long userId);

    @Query("SELECT p FROM Playlist p WHERE p.isPublic = true " +
            "AND (:hasDateFilter = false OR p.createdAt >= :dateFrom) " +
            "AND (:hasOwnerFilter = false OR LOWER(p.owner.username) LIKE LOWER(CONCAT('%', :ownerUsername, '%')))")
    List<Playlist> findPublicPlaylistsWithFilters(
            @Param("hasDateFilter") boolean hasDateFilter,
            @Param("dateFrom") java.time.LocalDateTime dateFrom,
            @Param("hasOwnerFilter") boolean hasOwnerFilter,
            @Param("ownerUsername") String ownerUsername,
            Sort sort);
}
