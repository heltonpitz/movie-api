package br.outsera.movies.model;


import lombok.Builder;

@Builder
public record MovieAwardsResultDTO(
    String producers,

    Integer interval,

    Integer previousWin,

    Integer followingWin
) {
}
