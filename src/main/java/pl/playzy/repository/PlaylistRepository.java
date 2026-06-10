package pl.playzy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.playzy.model.Playlist;
import pl.playzy.model.User;

import java.util.List;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    List<Playlist> findByOwner(User owner);
    List<Playlist> findByIsPublicTrue();
    List<Playlist> findByNameConntainingIgnoreCase(String name);
}
