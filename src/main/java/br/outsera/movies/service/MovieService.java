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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
                .min(getMinInterval(movieEntity))
                .max(getMaxInterval(movieEntity))
                .build())
            .defaultIfEmpty(MovieAwardsResultResponseDTO.builder().build());
    }

    private List<MovieAwardsResultDTO> getMinInterval(Map<String, List<MovieEntity>> movieEntity) {
        return movieEntity.entrySet().stream()
            .map(stringListEntry ->
            {
                var movie = stringListEntry.getValue().stream()
                    .toList();

                List<Long> diffs = new ArrayList<>();
                for (int i = 1; i < movie.size(); i++) {
                    diffs.add(ChronoUnit.YEARS.between(movie.get(i - 1).years(), movie.get(i).years()));
                }

                Long minInterval = Collections.min(diffs);
                int minIndex = diffs.indexOf(minInterval);

                return MovieAwardsResultDTO.builder()
                    .producers(stringListEntry.getKey())
                    .followingWin(movie.get(minIndex + 1).years())
                    .previousWin(minIndex < 0 ? movie.getFirst().years() : movie.get(minIndex).years())
                    .interval(minInterval.intValue())
                    .build();
            })
            .toList();
    }

    private static List<MovieAwardsResultDTO> getMaxInterval(Map<String, List<MovieEntity>> movieEntity) {
        return movieEntity.entrySet().stream()
            .map(stringListEntry ->
            {
                var movie = stringListEntry.getValue().stream()
                    .toList();

                List<Long> diffs = new ArrayList<>();
                for (int i = 1; i < movie.size(); i++) {
                    diffs.add(ChronoUnit.YEARS.between(movie.get(i - 1).years(), movie.get(i).years()));
                }

                Long maxInterval = Collections.max(diffs);
                int maxIndex = diffs.indexOf(maxInterval);

                return MovieAwardsResultDTO.builder()
                    .producers(stringListEntry.getKey())
                    .followingWin(movie.get(maxIndex + 1).years())
                    .previousWin(maxIndex < 0 ? movie.getLast().years() : movie.get(maxIndex).years())
                    .interval(maxInterval.intValue())
                    .build();
            })
            .toList();
    }
}
