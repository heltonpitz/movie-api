package br.outsera.movies.service;

import br.outsera.movies.model.MovieCsv;
import br.outsera.movies.repository.MovieRepository;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieService {

    @Value("${api.csv-file.separator}")
    public Character csvFileSeparator;
    @Value("${api.csv-file.path}")
    public String csvFilePath;

    final MovieRepository repository;

    public Mono<Void> initDB() {
        var movieEntities = getFileResource(new ClassPathResource(csvFilePath))
            .stream()
            .map(MovieCsv::toEntity)
            .toList();

        return repository.saveAll(movieEntities)
            .then();
    }

    private List<MovieCsv> getFileResource(final ClassPathResource resource) {
        try (Reader reader = new InputStreamReader(resource.getInputStream())) {
            return new CsvToBeanBuilder<MovieCsv>(reader)
                .withType(MovieCsv.class)
                .withIgnoreLeadingWhiteSpace(true)
                .withThrowExceptions(false)
                .withSeparator(csvFileSeparator)
                .build()
                .parse();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

}
