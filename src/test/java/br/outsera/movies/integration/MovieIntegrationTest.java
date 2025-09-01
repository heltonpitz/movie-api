package br.outsera.movies.integration;

import br.outsera.movies.Application;
import br.outsera.movies.repository.MovieRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

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

    @Test
    @DisplayName("Deve retornar resultado dos prêmios com dados reais")
    void deveRetornarResultadoPremiosComDadosReais() {

        webTestClient.get()
            .uri("/movie/awards-result")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.min").isArray()
            .jsonPath("$.max").isArray()
            .jsonPath("$.min[0]").exists()
            .jsonPath("$.min[0].producers").isEqualTo("Bo Derek")
            .jsonPath("$.min[0].interval").isEqualTo("6")
            .jsonPath("$.min[0].previousWin").isEqualTo("1984")
            .jsonPath("$.min[0].followingWin").isEqualTo("1990")
            .jsonPath("$.max[0]").exists()
            .jsonPath("$.max[0].producers").isEqualTo("Bo Derek")
            .jsonPath("$.max[0].interval").isEqualTo("6")
            .jsonPath("$.max[0].previousWin").isEqualTo("1984")
            .jsonPath("$.max[0].followingWin").isEqualTo("1990")
            .jsonPath("$.min[1]").doesNotExist()
            .jsonPath("$.max[1]").doesNotExist();
    }
}