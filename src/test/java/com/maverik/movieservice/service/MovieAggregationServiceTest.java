package com.maverik.movieservice.service;

import com.maverik.movieservice.client.ExternalMovieClient;
import com.maverik.movieservice.model.MovieEntity;
import com.maverik.movieservice.repository.MovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

class MovieAggregationServiceTest {

  @Mock
  private ExternalMovieClient externalMovieClient;

  @Mock
  private MovieRepository movieRepository;

  @InjectMocks
  private MovieAggregationService movieAggregationService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  private String sampleSearchResponse() {
    return """
            {
              "movies": [
                { "imdbId": "tt1234567" },
                { "imdbId": "tt2345678" }
              ]
            }
            """;
  }

  private String sampleMovieDetail(String imdbId) {
    return """
            {
              "title": "Sample Movie %s",
              "year": "2024",
              "actors": "Actor Name",
              "director": "Director Name",
              "imdbRating": "8.5",
              "poster": "http://poster.url/sample.jpg"
            }
            """.formatted(imdbId);
  }

  @Test
  void aggregateAndSaveMovies_shouldAggregateAndSaveSuccessfully() {
    // Mock searchMovieByTitle
    when(externalMovieClient.searchMovieByTitle(anyString()))
        .thenReturn(Mono.just(sampleSearchResponse()));

    // Mock getMovieDetailsByImdbId
    when(externalMovieClient.getMovieDetailsByImdbId(anyString()))
        .thenAnswer(invocation -> {
          String imdbId = invocation.getArgument(0);
          return Mono.just(sampleMovieDetail(imdbId));
        });

    // Mock repository save
    when(movieRepository.save(any(MovieEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

    movieAggregationService.aggregateAndSaveMovies();

    // Verify interactions
    verify(externalMovieClient, atLeastOnce()).searchMovieByTitle(anyString());
    verify(externalMovieClient, atLeastOnce()).getMovieDetailsByImdbId(anyString());
    verify(movieRepository, atLeastOnce()).save(any(MovieEntity.class));
  }

  @Test
  void parseImdbIds_shouldReturnCorrectList() throws Exception {
    String json = sampleSearchResponse();

    var method = MovieAggregationService.class.getDeclaredMethod("parseImdbIds", String.class);
    method.setAccessible(true);

    @SuppressWarnings("unchecked")
    List<String> result = (List<String>) method.invoke(movieAggregationService, json);

    assertThat(result).containsExactly("tt1234567", "tt2345678");
  }

  @Test
  void parseMovieDetails_shouldReturnValidMovieEntity() throws Exception {
    String json = sampleMovieDetail("tt1234567");

    var method = MovieAggregationService.class.getDeclaredMethod("parseMovieDetails", String.class);
    method.setAccessible(true);

    MovieEntity result = (MovieEntity) method.invoke(movieAggregationService, json);

    assertThat(result).isNotNull();
    assertThat(result.getTitle()).startsWith("Sample Movie");
    assertThat(result.getYear()).isEqualTo("2024");
    assertThat(result.getActor()).isEqualTo("Actor Name");
    assertThat(result.getDirector()).isEqualTo("Director Name");
  }
}
