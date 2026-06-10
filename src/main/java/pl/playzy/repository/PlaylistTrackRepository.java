package pl.playzy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.playzy.model.Playlist;
import pl.playzy.model.PlaylistTrack;

import java.util.List;
import java.util.Optional;

public interface PlaylistTrackRepository extends JpaRepository<PlaylistTrack, Long> {
    List<PlaylistTrack> findByPlaylist(Playlist playlist);

    List<PlaylistTrack> findByPlaylistOrderByAddedAtAsc(Playlist playlist);

    boolean existsByPlaylistAndSpotifyId(Playlist playlist, String spotifyId);

    Optional<PlaylistTrack> findByPlaylistAndSpotifyId(Playlist playlist, String spotifyId);

}
