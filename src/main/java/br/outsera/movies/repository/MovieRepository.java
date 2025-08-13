package br.outsera.movies.repository;

import br.outsera.movies.model.MovieEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieRepository extends ReactiveCrudRepository<MovieEntity, Long> {
}
