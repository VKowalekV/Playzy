package pl.playzy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import pl.playzy.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    @Query("SELECT p.owner FROM Playlist p WHERE p.isPublic = true GROUP BY p.owner ORDER BY COUNT(p) DESC")
    List<User> findAuthorsByPopularity();

    List<User> findAllByOrderByJoinDateDesc();

    List<User> findByUsernameContainingIgnoreCaseOrderByJoinDateDesc(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
