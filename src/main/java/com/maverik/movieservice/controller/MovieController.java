
package com.maverik.movieservice.controller;

import com.maverik.movieservice.dto.MovieDTO;
import com.maverik.movieservice.model.MovieEntity;
import com.maverik.movieservice.service.CsvExportService;
import com.maverik.movieservice.service.MovieService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;
    private final CsvExportService csvExportService;

    @PostMapping
    public ResponseEntity<MovieDTO> createMovie(@RequestBody MovieDTO movieDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(movieService.createMovie(movieDTO));
    }

    @GetMapping
    public ResponseEntity<Page<MovieDTO>> listMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy) {
        return ResponseEntity.ok(movieService.listMovies(page, size, sortBy));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieDTO> getMovie(@PathVariable Long id) {
        Optional<MovieDTO> movie = movieService.getMovieById(id);
        return movie.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<MovieDTO> updateMovie(@PathVariable Long id, @RequestBody MovieDTO movieDTO) {
        Optional<MovieDTO> updated = movieService.updateMovie(id, movieDTO);
        return updated.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/csv")
    public void downloadCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=movies.csv");

        List<MovieDTO> allMovies = movieService.listAllMovies();
        csvExportService.writeMoviesToCsv(
            allMovies.stream()
                .map(dto -> MovieEntity.builder()
                    .id(dto.id())
                    .title(dto.title())
                    .year(dto.year())
                    .actor(dto.actor())
                    .director(dto.director())
                    .rating(dto.rating())
                    .posterUrl(dto.posterUrl())
                    .build())
                .toList(),
            response.getWriter()
        );
    }

    @GetMapping("/poster/{id}")
    public ResponseEntity<String> getPosterUrl(@PathVariable Long id) {
        Optional<MovieDTO> movie = movieService.getMovieById(id);
        return movie.map(m -> ResponseEntity.ok(m.posterUrl()))
            .orElse(ResponseEntity.notFound().build());
    }
}
