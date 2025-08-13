package br.outsera.movies.controller;

import br.outsera.movies.model.MovieAwardsResultResponseDTO;
import br.outsera.movies.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;


@RequiredArgsConstructor
@RestController
@RequestMapping("/movie")
public class MovieController {

    private final MovieService movieService;


    @GetMapping("/awards-result")
    public Mono<MovieAwardsResultResponseDTO> getMovieAwardsResult() {
        return movieService.getMovieAwardsResult();
    }
}
