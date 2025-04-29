// src/test/java/com/maverik/movieservice/controller/MovieControllerTest.java
package com.maverik.movieservice.controller;

import com.maverik.movieservice.dto.MovieDTO;
import com.maverik.movieservice.service.CsvExportService;
import com.maverik.movieservice.service.MovieService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MovieControllerTest {

  private MockMvc mockMvc;

  @Mock
  private MovieService movieService;

  @Mock
  private CsvExportService csvExportService;

  @InjectMocks
  private MovieController movieController;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    mockMvc = MockMvcBuilders.standaloneSetup(movieController).build();
  }

  @Test
  void testCreateMovie() throws Exception {
    MovieDTO movieDTO = new MovieDTO(1L, "Movie Title", "2023", "Actor", "Director", "5", "posterUrl");
    when(movieService.createMovie(any(MovieDTO.class))).thenReturn(movieDTO);

    mockMvc.perform(post("/movies")
            .contentType("application/json")
            .content("{\"title\":\"Movie Title\",\"year\":\"2023\",\"actor\":\"Actor\",\"director\":\"Director\",\"rating\":\"5\",\"posterUrl\":\"posterUrl\"}")
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.title").value("Movie Title"));
  }

  @Test
  void testGetMovie() throws Exception {
    MovieDTO movieDTO = new MovieDTO(1L, "Movie Title", "2023", "Actor", "Director", "5", "posterUrl");
    when(movieService.getMovieById(1L)).thenReturn(Optional.of(movieDTO));

    mockMvc.perform(get("/movies/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Movie Title"));
  }

  @Test
  void testUpdateMovie() throws Exception {
    MovieDTO movieDTO = new MovieDTO(1L, "Updated Title", "2023", "Actor", "Director", "5", "posterUrl");
    when(movieService.updateMovie(anyLong(), any(MovieDTO.class))).thenReturn(Optional.of(movieDTO));

    mockMvc.perform(put("/movies/1")
            .contentType("application/json")
            .content("{\"title\":\"Updated Title\",\"year\":\"2023\",\"actor\":\"Actor\",\"director\":\"Director\",\"rating\":\"5\",\"posterUrl\":\"posterUrl\"}")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Updated Title"));
  }

  @Test
  void testDeleteMovie() throws Exception {
    mockMvc.perform(delete("/movies/1"))
        .andExpect(status().isNoContent());
  }
}
