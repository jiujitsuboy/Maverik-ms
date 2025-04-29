// src/test/java/com/maverik/movieservice/service/MovieServiceTest.java
package com.maverik.movieservice.service;

import com.maverik.movieservice.client.ExternalMovieClient;
import com.maverik.movieservice.dto.MovieDTO;
import com.maverik.movieservice.model.MovieEntity;
import com.maverik.movieservice.repository.MovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class MovieServiceTest {

  @Mock
  private MovieRepository movieRepository;

  @Mock
  private ExternalMovieClient externalMovieClient;

  @InjectMocks
  private MovieService movieService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  private MovieEntity createMovieEntity() {
    return MovieEntity.builder()
        .id(1L)
        .title("Inception")
        .year("2010")
        .actor("Leonardo DiCaprio")
        .director("Christopher Nolan")
        .rating("9")
        .posterUrl("some-url")
        .build();
  }

  private MovieDTO createMovieDTO() {
    return new MovieDTO(
        1L,
        "Inception",
        "2010",
        "Leonardo DiCaprio",
        "Christopher Nolan",
        "9",
        "some-url"
    );
  }

  @Test
  void createMovie_shouldSaveAndReturnMovie() {
    MovieDTO inputDto = createMovieDTO();
    MovieEntity savedEntity = createMovieEntity();

    when(movieRepository.save(any(MovieEntity.class))).thenReturn(savedEntity);

    MovieDTO result = movieService.createMovie(inputDto);

    assertThat(result).isNotNull();
    assertThat(result.title()).isEqualTo("Inception");
    verify(movieRepository, times(1)).save(any(MovieEntity.class));
  }

  @Test
  void listMovies_shouldReturnPagedMovies() {
    MovieEntity movieEntity = createMovieEntity();
    Page<MovieEntity> page = new PageImpl<>(List.of(movieEntity));

    when(movieRepository.findAll(any(PageRequest.class))).thenReturn(page);

    Page<MovieDTO> result = movieService.listMovies(0, 10, "title");

    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).title()).isEqualTo("Inception");
    verify(movieRepository, times(1)).findAll(any(PageRequest.class));
  }

  @Test
  void listAllMovies_shouldReturnAllMovies() {
    List<MovieEntity> movieEntities = Arrays.asList(createMovieEntity());

    when(movieRepository.findAll()).thenReturn(movieEntities);

    List<MovieDTO> result = movieService.listAllMovies();

    assertThat(result).isNotNull();
    assertThat(result).hasSize(1);
    assertThat(result.get(0).title()).isEqualTo("Inception");
    verify(movieRepository, times(1)).findAll();
  }

  @Test
  void getMovieById_shouldReturnMovieWhenFound() {
    MovieEntity movieEntity = createMovieEntity();

    when(movieRepository.findById(1L)).thenReturn(Optional.of(movieEntity));

    Optional<MovieDTO> result = movieService.getMovieById(1L);

    assertThat(result).isPresent();
    assertThat(result.get().title()).isEqualTo("Inception");
    verify(movieRepository, times(1)).findById(1L);
  }

  @Test
  void getMovieById_shouldReturnEmptyWhenNotFound() {
    when(movieRepository.findById(1L)).thenReturn(Optional.empty());

    Optional<MovieDTO> result = movieService.getMovieById(1L);

    assertThat(result).isEmpty();
    verify(movieRepository, times(1)).findById(1L);
  }

  @Test
  void updateMovie_shouldUpdateAndReturnMovieWhenFound() {
    MovieEntity movieEntity = createMovieEntity();
    MovieDTO updatedDto = new MovieDTO(
        1L,
        "Interstellar",
        "2014",
        "Matthew McConaughey",
        "Christopher Nolan",
        "10",
        "updated-url"
    );

    when(movieRepository.findById(1L)).thenReturn(Optional.of(movieEntity));

    Optional<MovieDTO> result = movieService.updateMovie(1L, updatedDto);

    assertThat(result).isPresent();
    assertThat(result.get().title()).isEqualTo("Interstellar");
    assertThat(movieEntity.getTitle()).isEqualTo("Interstellar"); // validate in-memory update
    verify(movieRepository, times(1)).findById(1L);
  }

  @Test
  void updateMovie_shouldReturnEmptyWhenMovieNotFound() {
    MovieDTO updatedDto = createMovieDTO();

    when(movieRepository.findById(1L)).thenReturn(Optional.empty());

    Optional<MovieDTO> result = movieService.updateMovie(1L, updatedDto);

    assertThat(result).isEmpty();
    verify(movieRepository, times(1)).findById(1L);
  }

  @Test
  void deleteMovie_shouldDeleteMovieById() {
    doNothing().when(movieRepository).deleteById(1L);

    movieService.deleteMovie(1L);

    verify(movieRepository, times(1)).deleteById(1L);
  }
}
