package br.outsera.movies.model;


import lombok.Builder;
import lombok.Generated;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Builder
@Table(name = "MOVIE")
public record MovieEntity(

    @Id
    @Generated
    Long id,

    @Column
    String years,

    @Column
    String title,

    @Column
    String studios,

    @Column
    String producers,

    @Column
    String winner

) {
}
