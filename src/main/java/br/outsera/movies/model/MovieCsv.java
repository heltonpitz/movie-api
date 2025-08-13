package br.outsera.movies.model;


import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MovieCsv {

    @CsvBindByName
    private String year;

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
            .title(this.getTitle())
            .studios(this.getStudios())
            .producers(this.getProducers())
            .winner(this.getWinner())
            .build();
    }
}
