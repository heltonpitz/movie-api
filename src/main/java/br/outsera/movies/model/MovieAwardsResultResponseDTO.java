package br.outsera.movies.model;


import lombok.Builder;

import java.util.List;

@Builder
public record MovieAwardsResultResponseDTO(
    List<MovieAwardsResultDTO> min,
    List<MovieAwardsResultDTO> max
) {
}
