package pl.playzy.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "playlists")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Playlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank
    @Size(min = 3, max = 100, message = "Nazwa playlisty musi mieć od 3 do 100 znaków")
    @Column(nullable = false)
    private String name;

    @Size(max = 500, message = "Opis może mieć maksymalnie 500 znaków")
    private String description;

    private boolean isPublic;

    public int getLikesCount() {
        if (ratings == null)
            return 0;
        return (int) ratings.stream().filter(PlaylistRating::isLike).count();
    }

    public int getDislikesCount() {
        if (ratings == null)
            return 0;
        return (int) ratings.stream().filter(r -> !r.isLike()).count();
    }

    public Boolean getUserRating(User user) {
        if (user == null || ratings == null) return null;
        return ratings.stream()
                .filter(r -> r.getUser().getId().equals(user.getId()))
                .map(PlaylistRating::isLike)
                .findFirst()
                .orElse(null);
    }

    public boolean isFollowedBy(User user) {
        if (user == null || followers == null) return false;
        return followers.stream().anyMatch(u -> u.getId().equals(user.getId()));
    }

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "playlist_followers", joinColumns = @JoinColumn(name = "playlist_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    @Builder.Default
    private Set<User> followers = new HashSet<>();

    @OneToMany(mappedBy = "playlist", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PlaylistRating> ratings = new java.util.ArrayList<>();

    @NotNull
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @OneToMany(mappedBy = "playlist", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PlaylistTrack> tracks = new java.util.ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "playlist_co_creators", joinColumns = @JoinColumn(name = "playlist_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    @Builder.Default
    private Set<User> coCreators = new HashSet<>();

    public double getTotalDurationMinutes() {
        if (tracks == null) return 0.0;
        return tracks.stream().mapToDouble(PlaylistTrack::getDurationMinutes).sum();
    }

    public String getTotalDurationFormatted() {
        if (tracks == null || tracks.isEmpty()) return "0 min";
        double totalMinutes = tracks.stream().mapToDouble(PlaylistTrack::getDurationMinutes).sum();
        long totalSeconds = Math.round(totalMinutes * 60);
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        
        if (hours > 0) {
            return String.format("%d godz. %d min", hours, minutes);
        } else {
            return String.format("%d min %d s", minutes, seconds);
        }
    }
}
