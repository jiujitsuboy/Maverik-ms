package com.maverik.movieservice.service;

import com.maverik.movieservice.model.MovieEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CsvExportService {

  public void writeMoviesToCsv(List<MovieEntity> movies, PrintWriter writer) {
    writer.println("Id,Title,Year,Actor,Director,Rating,PosterUrl");

    for (MovieEntity movie : movies) {
      writer.printf("%d,%s,%s,%s,%s,%s,%s\n",
          movie.getId(),
          escapeCsv(movie.getTitle()),
          escapeCsv(movie.getYear()),
          escapeCsv(movie.getActor()),
          escapeCsv(movie.getDirector()),
          escapeCsv(movie.getRating()),
          escapeCsv(movie.getPosterUrl()));
    }
  }

  private String escapeCsv(String value) {
    if (value == null) return "";
    if (value.contains(",") || value.contains("\"")) {
      value = value.replace("\"", "\"\"");
      value = "\"" + value + "\"";
    }
    return value;
  }
}
