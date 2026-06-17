package pl.playzy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.playzy.model.PlaylistTrack;

public interface PlaylistTrackRepository extends JpaRepository<PlaylistTrack, Long> {

}
