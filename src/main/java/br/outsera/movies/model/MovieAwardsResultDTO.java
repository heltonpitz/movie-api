package br.outsera.movies.model;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record MovieAwardsResultDTO(
    String producers,

    Integer interval,

    @JsonFormat(pattern = "yyyy")
    LocalDate previousWin,

    @JsonFormat(pattern = "yyyy")
    LocalDate followingWin
) {
}
