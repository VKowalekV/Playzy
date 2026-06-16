package pl.playzy.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import pl.playzy.dto.TopPlaylistDto;

@Service
public class TopPlaylistRestClientService {

    public TopPlaylistDto getTopPlaylist() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "http://localhost:8080/api/playlists/top";

            return restTemplate.getForObject(url, TopPlaylistDto.class);
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        } catch (Exception e) {
            System.err.println("Błąd pobierania top playlisty: " + e.getMessage());
            return null;
        }
    }
}
