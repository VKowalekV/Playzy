package pl.playzy.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
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
