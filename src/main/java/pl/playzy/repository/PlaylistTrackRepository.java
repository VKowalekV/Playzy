package pl.playzy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.playzy.model.Playlist;
import pl.playzy.model.PlaylistTrack;

import java.util.List;

public interface PlaylistTrackRepository extends JpaRepository<PlaylistTrack, Long> {
    List<PlaylistTrack> findByPlaylist(Playlist playlist);
}
