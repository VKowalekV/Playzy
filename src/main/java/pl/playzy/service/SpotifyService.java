package pl.playzy.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.playzy.dto.TrackDto;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchTracksRequest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SpotifyService {

    @Value("${SPOTIFY_CLIENT_ID}")
    private String clientId;

    @Value("${SPOTIFY_CLIENT_SECRET}")
    private String clientSecret;

    private SpotifyApi spotifyApi;

    @PostConstruct
    public void init() {
        spotifyApi = new SpotifyApi.Builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .build();
    }

    private LocalDateTime tokenExpiry;

    private synchronized void ensureToken() {
        if (tokenExpiry == null || LocalDateTime.now().isAfter(tokenExpiry)) {
            try {
                ClientCredentialsRequest request = spotifyApi.clientCredentials().build();
                ClientCredentials credentials = request.execute();
                spotifyApi.setAccessToken(credentials.getAccessToken());
                // Token ważny przez 1 godzinę, dajemy bufor 60 sekund
                tokenExpiry = LocalDateTime.now().plusSeconds(credentials.getExpiresIn() - 60);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public List<TrackDto> searchTracks(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        ensureToken();

        try {
            SearchTracksRequest searchTracksRequest = spotifyApi.searchTracks(query)
                    .limit(10)
                    .build();

            Paging<Track> trackPaging = searchTracksRequest.execute();

            return Arrays.stream(trackPaging.getItems())
                    .map(t -> TrackDto.builder()
                            .id(t.getId())
                            .title(t.getName())
                            .artist(t.getArtists().length > 0 ? t.getArtists()[0].getName() : "Nieznany")
                            .albumImageUrl(
                                    t.getAlbum().getImages().length > 0 ? t.getAlbum().getImages()[0].getUrl() : null)
                            .durationMinutes(t.getDurationMs() / 60000.0)
                            .previewUrl(t.getPreviewUrl())
                            .build())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
