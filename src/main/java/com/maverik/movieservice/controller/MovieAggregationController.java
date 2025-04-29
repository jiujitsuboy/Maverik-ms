package com.maverik.movieservice.controller;

import com.maverik.movieservice.service.MovieAggregationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/movies/aggregate")
@RequiredArgsConstructor
public class MovieAggregationController {

  private final MovieAggregationService aggregationService;

  @GetMapping
  public ResponseEntity<String> triggerAggregation() {
    aggregationService.aggregateAndSaveMovies();
    return ResponseEntity.ok("Aggregation started");
  }
}