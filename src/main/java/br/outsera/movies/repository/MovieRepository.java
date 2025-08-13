package br.outsera.movies.repository;

import br.outsera.movies.model.MovieEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface MovieRepository extends ReactiveCrudRepository<MovieEntity, Long> {

    @Query(value = """
        SELECT * from MOVIE
        WHERE winner = TRUE
        ORDER BY years;
        """)
    Flux<MovieEntity> getMovieAwardsResult();

}
