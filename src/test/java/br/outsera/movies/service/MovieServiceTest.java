package br.outsera.movies.service;

import br.outsera.movies.model.MovieEntity;
import br.outsera.movies.repository.MovieRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.Arrays;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MovieService Tests")
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private MovieService movieService;

    private MovieEntity createMovie(String title, Integer year, String producers, Boolean winner) {
        return MovieEntity.builder()
            .title(title)
            .years(LocalDate.of(year, 1, 1))
            .producers(producers)
            .winner(winner)
            .build();
    }

    @Test
    @DisplayName("Deve calcular intervalos dos prêmios corretamente")
    void deveCalcularIntervalosPremiosCorretamente() {
        // Given
        MovieEntity movie1 = createMovie("Movie 1", 2000, "Producer A", true);
        MovieEntity movie2 = createMovie("Movie 2", 2001, "Producer A", true);
        MovieEntity movie3 = createMovie("Movie 3", 1990, "Producer B", true);
        MovieEntity movie4 = createMovie("Movie 4", 2003, "Producer B", true);
        MovieEntity movie5 = createMovie("Movie 5", 2002, "Producer C", false);

        Flux<MovieEntity> movieFlux = Flux.fromIterable(
            Arrays.asList(movie1, movie2, movie3, movie4, movie5)
        );

        when(movieRepository.getMovieAwardsResult()).thenReturn(movieFlux);

        // When & Then
        StepVerifier.create(movieService.getMovieAwardsResult())
            .expectNextMatches(result -> {
                assert result.min().size() == 2;
                assert result.max().size() == 2;
                assert result.min().getFirst().producers().equals("Producer A");
                assert result.min().getFirst().interval() == 1;
                assert result.min().getFirst().previousWin().getYear() == 2000;
                assert result.min().getFirst().followingWin().getYear() == 2001;
                assert result.max().getFirst().producers().equals("Producer A");
                assert result.max().getFirst().interval() == 1;
                assert result.max().getFirst().previousWin().getYear() == 2000;
                assert result.max().getFirst().followingWin().getYear() == 2001;
                assert result.min().getLast().producers().equals("Producer B");
                assert result.min().getLast().interval() == 13;
                assert result.min().getLast().previousWin().getYear() == 1990;
                assert result.min().getLast().followingWin().getYear() == 2003;
                assert result.max().getLast().producers().equals("Producer B");
                assert result.max().getLast().interval() == 13;
                assert result.max().getLast().previousWin().getYear() == 1990;
                assert result.max().getLast().followingWin().getYear() == 2003;
                return true;
            })
            .verifyComplete();

        verify(movieRepository, times(1)).getMovieAwardsResult();
    }

    @Test
    @DisplayName("Deve retornar resultado vazio quando não há filmes vencedores")
    void deveRetornarResultadoVazioQuandoNaoHaFilmesVencedores() {
        // Given
        MovieEntity movie1 = createMovie("Movie 1", 2000, "Producer A", false);
        MovieEntity movie2 = createMovie("Movie 2", 2001, "Producer B", false);

        Flux<MovieEntity> movieFlux = Flux.fromIterable(Arrays.asList(movie1, movie2));

        when(movieRepository.getMovieAwardsResult()).thenReturn(movieFlux);

        // When & Then
        StepVerifier.create(movieService.getMovieAwardsResult())
            .expectNextMatches(result -> {
                assert result.min().isEmpty();
                assert result.max().isEmpty();
                return true;
            })
            .verifyComplete();

        verify(movieRepository, times(1)).getMovieAwardsResult();
    }

    @Test
    @DisplayName("Deve lidar com produtor que ganhou apenas um prêmio")
    void deveLidarComProdutorQueGanhouApenasUmPremio() {
        // Given
        MovieEntity movie1 = createMovie("Movie 1", 2000, "Producer A", true);
        MovieEntity movie2 = createMovie("Movie 2", 2001, "Producer B", true);
        MovieEntity movie3 = createMovie("Movie 3", 2002, "Producer C", true);

        Flux<MovieEntity> movieFlux = Flux.fromIterable(Arrays.asList(movie1, movie2, movie3));

        when(movieRepository.getMovieAwardsResult()).thenReturn(movieFlux);

        // When & Then
        StepVerifier.create(movieService.getMovieAwardsResult())
            .expectNextMatches(result -> {
                assert result.min().size() == 0;
                assert result.max().size() == 0;
                return true;
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Deve retornar múltiplos resultados em caso de empate")
    void deveRetornarMultiplosResultadosEmCasoDeEmpate() {
        // Given
        MovieEntity movie1 = createMovie("Movie 1", 2000, "Producer A", true);
        MovieEntity movie2 = createMovie("Movie 2", 2001, "Producer A", true);
        MovieEntity movie3 = createMovie("Movie 3", 2005, "Producer B", true);
        MovieEntity movie4 = createMovie("Movie 4", 2006, "Producer B", true);

        Flux<MovieEntity> movieFlux = Flux.fromIterable(
            Arrays.asList(movie1, movie2, movie3, movie4)
        );

        when(movieRepository.getMovieAwardsResult()).thenReturn(movieFlux);

        // When & Then
        StepVerifier.create(movieService.getMovieAwardsResult())
            .expectNextMatches(result -> {
                assert result.min().size() == 2; // Empate no mínimo
                assert result.min().stream()
                    .allMatch(interval -> interval.interval() == 1);
                return true;
            })
            .verifyComplete();
    }

}