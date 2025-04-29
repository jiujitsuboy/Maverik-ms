package com.maverik.movieservice.service;

import com.maverik.movieservice.client.ExternalMovieClient;
import com.maverik.movieservice.model.MovieEntity;
import com.maverik.movieservice.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;


@Service
@RequiredArgsConstructor
@Slf4j
public class MovieAggregationService {

  private final ExternalMovieClient externalMovieClient;
  private final MovieRepository movieRepository;

  private final ObjectMapper objectMapper = new ObjectMapper();

  private static final List<String> SEARCH_KEYWORDS = List.of("action", "comedy", "drama", "thriller", "sci-fi");

//  @PostConstruct
  public void fetchMoviesAtStartup() {
    aggregateAndSaveMovies();
  }

  @Async
  public void aggregateAndSaveMovies() {
    Set<String> imdbIds = ConcurrentHashMap.newKeySet();

    List<CompletableFuture<Void>> futures = SEARCH_KEYWORDS.stream()
        .map(keyword -> externalMovieClient.searchMovieByTitle(keyword)
            .toFuture()
            .thenAccept(result -> {
              // Parse the result and collect imdbIds
              List<String> ids = parseImdbIds(result);
              imdbIds.addAll(ids);
            }))
        .toList();

    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

    // Limit to 100 unique movies
    List<String> limitedIds = imdbIds.stream().limit(100).toList();

    List<CompletableFuture<Void>> detailFutures = limitedIds.stream()
        .map(imdbId -> externalMovieClient.getMovieDetailsByImdbId(imdbId)
            .toFuture()
            .thenAccept(details -> {
              MovieEntity entity = parseMovieDetails(details);
              if (entity != null) {
                movieRepository.save(entity);
              }
            }))
        .collect(Collectors.toList());

    CompletableFuture.allOf(detailFutures.toArray(new CompletableFuture[0])).join();

    log.info("Aggregated and saved {} movies.", limitedIds.size());
  }

  @SneakyThrows
  private List<String> parseImdbIds(String json) {
    JsonNode root = objectMapper.readTree(json);
    if (!root.has("movies")) return Collections.emptyList();

    List<String> ids = new ArrayList<>();
    for (JsonNode movieNode : root.get("movies")) {
      if (movieNode.has("imdbId")) {
        ids.add(movieNode.get("imdbId").asText());
      }
    }
    return ids;
  }

  @SneakyThrows
  private MovieEntity parseMovieDetails(String json) {
    JsonNode root = objectMapper.readTree(json);

    return MovieEntity.builder()
        .title(root.path("title").asText())
        .year(root.path("year").asText())
        .actor(root.path("actors").asText())
        .director(root.path("director").asText())
        .rating(root.path("imdbRating").asText())
        .posterUrl(root.path("poster").asText())
        .build();
  }
}
