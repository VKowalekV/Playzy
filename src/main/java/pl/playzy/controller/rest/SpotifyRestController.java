package pl.playzy.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.playzy.dto.TrackDto;
import pl.playzy.service.SpotifyService;

import java.util.List;

@RestController
@RequestMapping("/api/spotify")
@RequiredArgsConstructor
public class SpotifyRestController {

    private final SpotifyService spotifyService;

    @GetMapping("/search")
    public ResponseEntity<List<TrackDto>> searchTracks(@RequestParam String q) {
        return ResponseEntity.ok(spotifyService.searchTracks(q));
    }
}
