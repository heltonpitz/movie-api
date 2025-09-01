package br.outsera.movies.service;

import br.outsera.movies.model.MovieAwardsResultDTO;
import br.outsera.movies.model.MovieAwardsResultResponseDTO;
import br.outsera.movies.model.MovieCsv;
import br.outsera.movies.model.MovieEntity;
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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    public Mono<MovieAwardsResultResponseDTO> getMovieAwardsResult() {

        return repository.getMovieAwardsResult()
            .collect(Collectors.groupingBy(MovieEntity::producers))
            .map(stringListMap -> stringListMap.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
            .map(movieEntity -> MovieAwardsResultResponseDTO.builder()
                .min(getMinIntervalResult(calculateIntervals(movieEntity)))
                .max(getMaxIntervalResult(calculateIntervals(movieEntity)))
                .build())
            .defaultIfEmpty(MovieAwardsResultResponseDTO.builder().build());

    }

    private List<MovieAwardsResultDTO> calculateIntervals(Map<String, List<MovieEntity>> movieEntity) {
        return movieEntity.values().stream()
            .flatMap(movieEntities ->
            {
                var sortedMovies = movieEntities.stream()
                    .sorted(Comparator.comparingInt(MovieEntity::years))
                    .toList();

                return IntStream.range(1, sortedMovies.size())
                    .mapToObj(movieIndex -> {
                        var previousMovie = sortedMovies.get(movieIndex - 1);
                        var currentMovie = sortedMovies.get(movieIndex);
                        int interval = currentMovie.years() - previousMovie.years();
                        return MovieAwardsResultDTO.builder()
                            .producers(currentMovie.producers())
                            .previousWin(previousMovie.years())
                            .followingWin(currentMovie.years())
                            .interval(interval)
                            .build();
                    });

            })
            .toList();
    }

    private List<MovieAwardsResultDTO> getMaxIntervalResult(List<MovieAwardsResultDTO> movieAwardResults) {
        return movieAwardResults.stream()
            .filter(movieAwardResult -> movieAwardResults.stream()
                .max(Comparator.comparingInt(MovieAwardsResultDTO::interval)).stream()
                .findAny()
                .orElse(MovieAwardsResultDTO.builder().build()).interval()
                .equals(movieAwardResult.interval()))
            .toList();
    }

    private List<MovieAwardsResultDTO> getMinIntervalResult(List<MovieAwardsResultDTO> movieAwardResults) {
        return movieAwardResults.stream()
            .filter(movieAwardResult -> movieAwardResults.stream()
                .min(Comparator.comparingInt(MovieAwardsResultDTO::interval)).stream()
                .findAny()
                .orElse(MovieAwardsResultDTO.builder().build()).interval()
                .equals(movieAwardResult.interval()))
            .toList();
    }
}
