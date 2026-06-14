package pl.playzy.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pl.playzy.dto.TopPlaylistDto;
import pl.playzy.service.TopPlaylistRestClientService;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final TopPlaylistRestClientService topPlaylistRestClientService;

    @GetMapping("/")
    public String home(Model model) {
        TopPlaylistDto topPlaylist = topPlaylistRestClientService.getTopPlaylist();
        if (topPlaylist != null) {
            model.addAttribute("topPlaylist", topPlaylist);
        }
        return "index";
    }
}
