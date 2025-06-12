package com.showsync.repository;

import com.showsync.entity.Media;
import com.showsync.entity.Media.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class MediaRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MediaRepository mediaRepository;

    @Test
    public void whenSaveMedia_thenMediaIsPersisted() {
        // Create a test media
        Media media = new Media();
        media.setType(MediaType.MOVIE);
        media.setTitle("Test Movie");
        media.setDescription("A test movie description");
        media.setReleaseDate(LocalDateTime.now());
        media.setExternalId("tmdb123");
        media.setExternalSource("TMDB");
        media.setCreatedAt(LocalDateTime.now());
        media.setUpdatedAt(LocalDateTime.now());

        // Save the media
        Media savedMedia = mediaRepository.save(media);

        // Verify the media was saved
        assertThat(savedMedia.getId()).isNotNull();
        
        // Retrieve the media
        Optional<Media> foundMedia = mediaRepository.findById(savedMedia.getId());
        
        // Verify the retrieved media
        assertThat(foundMedia).isPresent();
        assertThat(foundMedia.get().getTitle()).isEqualTo("Test Movie");
        assertThat(foundMedia.get().getType()).isEqualTo(MediaType.MOVIE);
    }

    @Test
    public void whenFindByType_thenReturnMediaList() {
        // Create and save test media
        Media movie = new Media();
        movie.setType(MediaType.MOVIE);
        movie.setTitle("Test Movie");
        movie.setCreatedAt(LocalDateTime.now());
        movie.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(movie);

        Media tvShow = new Media();
        tvShow.setType(MediaType.TV_SHOW);
        tvShow.setTitle("Test TV Show");
        tvShow.setCreatedAt(LocalDateTime.now());
        tvShow.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(tvShow);

        entityManager.flush();

        // Find all movies
        List<Media> movies = mediaRepository.findByType(MediaType.MOVIE);

        // Verify the results
        assertThat(movies).hasSize(1);
        assertThat(movies.get(0).getType()).isEqualTo(MediaType.MOVIE);
    }

    @Test
    public void whenFindByExternalIdAndSource_thenReturnMedia() {
        // Create and save test media
        Media media = new Media();
        media.setType(MediaType.MOVIE);
        media.setTitle("Test Movie");
        media.setExternalId("tmdb123");
        media.setExternalSource("TMDB");
        media.setCreatedAt(LocalDateTime.now());
        media.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(media);
        entityManager.flush();

        // Find the media by external ID and source
        Optional<Media> foundMedia = mediaRepository.findByExternalIdAndExternalSource("tmdb123", "TMDB");

        // Verify the media was found
        assertThat(foundMedia).isPresent();
        assertThat(foundMedia.get().getExternalId()).isEqualTo("tmdb123");
        assertThat(foundMedia.get().getExternalSource()).isEqualTo("TMDB");
    }
} 