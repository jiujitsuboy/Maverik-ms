package com.maverik.movieservice.controller;

import com.maverik.movieservice.dto.MovieDTO;
import com.maverik.movieservice.model.MovieEntity;
import com.maverik.movieservice.repository.MovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MovieControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private MovieRepository movieRepository;

  private Long movieId;

  @BeforeEach
  void setUp() {
    // Insert a movie into the database for testing
    MovieDTO movieDTO = new MovieDTO(null, "Inception", "2010", "Leonardo DiCaprio", "Christopher Nolan", "9", "posterUrl");
    movieRepository.saveAndFlush(toEntity(movieDTO));
    movieId = movieDTO.id(); // Assuming getId() returns the generated ID
  }

  @Disabled
  @Test
  void testCreateMovie() throws Exception {
    mockMvc.perform(post("/movies")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"title\":\"The Dark Knight\",\"year\":\"2008\",\"actor\":\"Christian Bale\",\"director\":\"Christopher Nolan\",\"rating\":\"9\",\"posterUrl\":\"posterUrl\"}")
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.title").value("The Dark Knight"));
  }

  @Disabled
  @Test
  void testGetMovie() throws Exception {
    mockMvc.perform(get("/movies/{id}", movieId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Inception"));
  }

  @Disabled
  @Test
  void testUpdateMovie() throws Exception {
    mockMvc.perform(put("/movies/{id}", movieId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"title\":\"Updated Title\",\"year\":\"2023\",\"actor\":\"Actor\",\"director\":\"Director\",\"rating\":\"5\",\"posterUrl\":\"posterUrl\"}")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Updated Title"));
  }

  @Disabled
  @Test
  void testDeleteMovie() throws Exception {
    mockMvc.perform(delete("/movies/{id}", movieId))
        .andExpect(status().isNoContent());

    // Verify movie is deleted from the DB
    mockMvc.perform(get("/movies/{id}", movieId))
        .andExpect(status().isNotFound());
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
