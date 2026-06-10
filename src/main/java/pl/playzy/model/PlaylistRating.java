package pl.playzy.model;

import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "playlist_ratings", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "playlist_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"user", "playlist"})
@ToString(exclude = {"user", "playlist"})
public class PlaylistRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playlist_id", nullable = false)
    private Playlist playlist;

    @Column(nullable = false)
    private boolean isLike;
}
