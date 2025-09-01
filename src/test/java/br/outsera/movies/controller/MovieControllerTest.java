package br.outsera.movies.controller;

import br.outsera.movies.model.MovieAwardsResultDTO;
import br.outsera.movies.model.MovieAwardsResultResponseDTO;
import br.outsera.movies.service.MovieService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MovieController Tests")
class MovieControllerTest {

    @Mock
    private MovieService movieService;

    @InjectMocks
    private MovieController movieController;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient
            .bindToController(movieController)
            .build();
    }

    @Test
    @DisplayName("Deve retornar resultado dos prêmios com sucesso")
    void deveRetornarResultadoPremiosComSucesso() {
        // Given
        var minInterval = MovieAwardsResultDTO.builder()
            .producers("Producer A")
            .interval(1)
            .previousWin(2000)
            .followingWin(2001)
            .build();

        var maxInterval = MovieAwardsResultDTO.builder()
            .producers("Producer B")
            .interval(13)
            .previousWin(1990)
            .followingWin(2003)
            .build();

        var expectedResult = MovieAwardsResultResponseDTO.builder()
            .min(Collections.singletonList(minInterval))
            .max(Collections.singletonList(maxInterval))
            .build();

        when(movieService.getMovieAwardsResult()).thenReturn(Mono.just(expectedResult));

        // When & Then
        webTestClient.get()
            .uri("/movie/awards-result")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.min").isArray()
            .jsonPath("$.max").isArray()
            .jsonPath("$.min[0].producers").isEqualTo("Producer A")
            .jsonPath("$.min[0].interval").isEqualTo("1")
            .jsonPath("$.max[0].producers").isEqualTo("Producer B")
            .jsonPath("$.max[0].interval").isEqualTo("13")
            .jsonPath("$.min[1]").doesNotExist()
            .jsonPath("$.max[1]").doesNotExist();

        verify(movieService, times(1)).getMovieAwardsResult();
    }

    @Test
    @DisplayName("Deve retornar resultado vazio quando não há dados")
    void deveRetornarResultadoVazioQuandoNaoHaDados() {
        // Given
        MovieAwardsResultResponseDTO emptyResult = MovieAwardsResultResponseDTO.builder()
            .min(Collections.emptyList())
            .max(Collections.emptyList())
            .build();

        when(movieService.getMovieAwardsResult()).thenReturn(Mono.just(emptyResult));

        // When & Then
        webTestClient.get()
            .uri("/movie/awards-result")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.min").isArray()
            .jsonPath("$.max").isArray()
            .jsonPath("$.min[0]").doesNotExist()
            .jsonPath("$.max[0]").doesNotExist();

        verify(movieService, times(1)).getMovieAwardsResult();
    }

    @Test
    @DisplayName("Deve retornar múltiplos resultados quando há empate")
    void deveRetornarMultiplosResultadosQuandoHaEmpate() {
        // Given
        var minInterval1 = MovieAwardsResultDTO.builder()
            .producers("Producer A")
            .interval(1)
            .previousWin(2000)
            .followingWin(2001)
            .build();

        var minInterval2 = MovieAwardsResultDTO.builder()
            .producers("Producer C")
            .interval(1)
            .previousWin(2005)
            .followingWin(2006)
            .build();

        var maxInterval = MovieAwardsResultDTO.builder()
            .producers("Producer B")
            .interval(13)
            .previousWin(1990)
            .followingWin(2003)
            .build();

        var expectedResult = MovieAwardsResultResponseDTO.builder()
            .min(Arrays.asList(minInterval1, minInterval2))
            .max(Collections.singletonList(maxInterval))
            .build();

        when(movieService.getMovieAwardsResult()).thenReturn(Mono.just(expectedResult));

        // When & Then
        webTestClient.get()
            .uri("/movie/awards-result")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.min").isArray()
            .jsonPath("$.max").isArray()
            .jsonPath("$.min[0]").exists()
            .jsonPath("$.max[0]").exists()
            .jsonPath("$.min[1]").exists()
            .jsonPath("$.max[1]").doesNotExist()
            .jsonPath("$.min[2]").doesNotExist()
            .jsonPath("$.max[2]").doesNotExist();

        verify(movieService, times(1)).getMovieAwardsResult();
    }

    @Test
    @DisplayName("Deve lidar com erro no serviço")
    void deveLidarComErroNoServico() {
        // Given
        when(movieService.getMovieAwardsResult())
            .thenReturn(Mono.error(new RuntimeException("Erro interno do serviço")));

        // When & Then
        webTestClient.get()
            .uri("/movie/awards-result")
            .exchange()
            .expectStatus().is5xxServerError();

        verify(movieService, times(1)).getMovieAwardsResult();
    }
}