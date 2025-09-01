package br.outsera.movies.model;


import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.logging.log4j.util.Strings;

import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MovieCsv {

    @CsvBindByName
    private Integer year;

    @CsvBindByName
    private String title;

    @CsvBindByName
    private String studios;

    @CsvBindByName
    private String producers;

    @CsvBindByName
    private String winner;

    public MovieEntity toEntity() {
        return MovieEntity.builder()
            .years(this.getYear())
            .title(Objects.requireNonNullElse(this.getTitle(), Strings.EMPTY))
            .studios(Objects.requireNonNullElse(this.getStudios(), Strings.EMPTY))
            .producers(Objects.requireNonNullElse(this.getProducers(), Strings.EMPTY))
            .winner("YES".compareToIgnoreCase(this.getWinner()) == 0)
            .build();
    }
}
