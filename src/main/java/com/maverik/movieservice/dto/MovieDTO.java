
package com.maverik.movieservice.dto;

public record MovieDTO(
    Long id,
    String title,
    String year,
    String actor,
    String director,
    String rating,
    String posterUrl
) {}
