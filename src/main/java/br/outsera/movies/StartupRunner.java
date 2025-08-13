package br.outsera.movies;

import br.outsera.movies.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StartupRunner implements ApplicationRunner {

    private final MovieService movieService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        movieService.initDB()
            .subscribe();
    }
}