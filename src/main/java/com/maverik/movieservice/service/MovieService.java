
package com.maverik.movieservice.service;

import com.maverik.movieservice.client.ExternalMovieClient;
import com.maverik.movieservice.dto.MovieDTO;
import com.maverik.movieservice.model.MovieEntity;
import com.maverik.movieservice.repository.MovieRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieService {

    private final MovieRepository movieRepository;
    private final ExternalMovieClient externalMovieClient;

    public MovieDTO createMovie(MovieDTO dto) {
        MovieEntity entity = toEntity(dto);
        return toDto(movieRepository.save(entity));
    }

    public Page<MovieDTO> listMovies(int page, int size, String sortBy) {
        return movieRepository.findAll(PageRequest.of(page, size, Sort.by(sortBy)))
                .map(this::toDto);
    }

  public List<MovieDTO> listAllMovies() {
    return movieRepository.findAll()
        .stream()
        .map(this::toDto)
        .collect(Collectors.toList());
  }
    public Optional<MovieDTO> getMovieById(Long id) {
        return movieRepository.findById(id).map(this::toDto);
    }

    @Transactional
    public Optional<MovieDTO> updateMovie(Long id, MovieDTO updatedDto) {
        return movieRepository.findById(id)
                .map(existing -> {
                    existing.setTitle(updatedDto.title());
                    existing.setYear(updatedDto.year());
                    existing.setActor(updatedDto.actor());
                    existing.setDirector(updatedDto.director());
                    existing.setRating(updatedDto.rating());
                    existing.setPosterUrl(updatedDto.posterUrl());
                    return toDto(existing);
                });
    }

    public void deleteMovie(Long id) {
        movieRepository.deleteById(id);
    }

    private MovieDTO toDto(MovieEntity entity) {
        return new MovieDTO(
                entity.getId(),
                entity.getTitle(),
                entity.getYear(),
                entity.getActor(),
                entity.getDirector(),
                entity.getRating(),
                entity.getPosterUrl()
        );
    }

    private MovieEntity toEntity(MovieDTO dto) {
        return MovieEntity.builder()
                .id(dto.id())
                .title(dto.title())
                .year(dto.year())
                .actor(dto.actor())
                .director(dto.director())
                .rating(dto.rating())
                .posterUrl(dto.posterUrl())
                .build();
    }
}
