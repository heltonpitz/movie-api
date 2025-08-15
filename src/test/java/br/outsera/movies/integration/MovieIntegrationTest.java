package br.outsera.movies.integration;

import br.outsera.movies.Application;
import br.outsera.movies.model.MovieAwardsResultResponseDTO;
import br.outsera.movies.model.MovieEntity;
import br.outsera.movies.repository.MovieRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@DisplayName("Testes de Integração - Movie API")
class MovieIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        movieRepository.deleteAll().block();
    }

    private MovieEntity createMovie(String title, Integer year, String producers, Boolean winner) {
        return MovieEntity.builder()
            .title(title)
            .years(LocalDate.of(year, 1, 1))
            .producers(producers)
            .winner(winner)
            .build();
    }

    @Test
    @DisplayName("Deve retornar resultado dos prêmios com dados reais")
    void deveRetornarResultadoPremiosComDadosReais() {
        var movie1 = createMovie("Movie A", 1980, "Producer 1", true);
        var movie2 = createMovie("Movie B", 1981, "Producer 1", true);
        var movie3 = createMovie("Movie C", 1982, "Producer 2", true);
        var movie4 = createMovie("Movie D", 1995, "Producer 2", true);
        var movie5 = createMovie("Movie E", 1983, "Producer 3", false);

        Mono.when(
            movieRepository.save(movie1),
            movieRepository.save(movie2),
            movieRepository.save(movie3),
            movieRepository.save(movie4),
            movieRepository.save(movie5)
        ).block();

        webTestClient.get()
            .uri("/movie/awards-result")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.min").isArray()
            .jsonPath("$.max").isArray()
            .jsonPath("$.min[0]").exists()
            .jsonPath("$.min[0].producers").isEqualTo("Producer 1")
            .jsonPath("$.min[0].interval").isEqualTo("1")
            .jsonPath("$.min[1].producers").isEqualTo("Producer 2")
            .jsonPath("$.min[1].interval").isEqualTo("13")
            .jsonPath("$.max[0]").exists()
            .jsonPath("$.max[0].producers").isEqualTo("Producer 1")
            .jsonPath("$.max[0].interval").isEqualTo("1")
            .jsonPath("$.max[1].producers").isEqualTo("Producer 2")
            .jsonPath("$.max[1].interval").isEqualTo("13")
            .jsonPath("$.min[2]").doesNotExist()
            .jsonPath("$.max[2]").doesNotExist();
    }

    @Test
    @DisplayName("Deve retornar resultado vazio quando não há filmes vencedores")
    void deveRetornarResultadoVazioQuandoNaoHaFilmesVencedores() {
        var movie1 = createMovie("Movie A", 1980, "Producer 1", false);
        var movie2 = createMovie("Movie B", 1981, "Producer 2", false);

        Mono.when(
            movieRepository.save(movie1),
            movieRepository.save(movie2)
        ).block();

        webTestClient.get()
            .uri("/movie/awards-result")
            .exchange()
            .expectStatus().isOk()
            .expectBody(MovieAwardsResultResponseDTO.class)
            .value(result -> {
                assert result != null;
                assert result.min().isEmpty();
                assert result.max().isEmpty();
            });
    }

    @Test
    @DisplayName("Deve retornar múltiplos resultados em caso de empate")
    void deveRetornarMultiplosResultadosEmCasoDeEmpate() {
        var movie1 = createMovie("Movie A", 1980, "Producer A", true);
        var movie2 = createMovie("Movie B", 1981, "Producer A", true);
        var movie3 = createMovie("Movie C", 1985, "Producer B", true);
        var movie4 = createMovie("Movie D", 1986, "Producer B", true);

        Mono.when(
            movieRepository.save(movie1),
            movieRepository.save(movie2),
            movieRepository.save(movie3),
            movieRepository.save(movie4)
        ).block();

        webTestClient.get()
            .uri("/movie/awards-result")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.min").isArray()
            .jsonPath("$.max").isArray()
            .jsonPath("$.min[0]").exists()
            .jsonPath("$.min[0].producers").isEqualTo("Producer A")
            .jsonPath("$.min[0].interval").isEqualTo("1")
            .jsonPath("$.min[1].producers").isEqualTo("Producer B")
            .jsonPath("$.min[1].interval").isEqualTo("1")
            .jsonPath("$.max[0]").exists()
            .jsonPath("$.max[0].producers").isEqualTo("Producer A")
            .jsonPath("$.max[0].interval").isEqualTo("1")
            .jsonPath("$.max[1].producers").isEqualTo("Producer B")
            .jsonPath("$.max[1].interval").isEqualTo("1")
            .jsonPath("$.min[2]").doesNotExist()
            .jsonPath("$.max[2]").doesNotExist();
    }

    @Test
    @DisplayName("Deve validar estrutura JSON da resposta")
    void deveValidarEstruturaJSONDaResposta() throws JsonProcessingException {
        MovieEntity movie1 = createMovie("Movie A", 1980, "Producer A", true);
        MovieEntity movie2 = createMovie("Movie B", 1981, "Producer A", true);

        Mono.when(
            movieRepository.save(movie1),
            movieRepository.save(movie2)
        ).block();

        webTestClient.get()
            .uri("/movie/awards-result")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.min").isArray()
            .jsonPath("$.max").isArray()
            .jsonPath("$.min[0].producers").exists()
            .jsonPath("$.min[0].interval").isNumber()
            .jsonPath("$.min[0].previousWin").exists()
            .jsonPath("$.min[0].followingWin").exists();
    }
}