package com.vmanam.recomendr.entities;

import lombok.Data;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.List;

@Data
@Table("movie_by_id")
public class MovieEntity {
    @PrimaryKeyColumn(type=PrimaryKeyType.PARTITIONED)
    private String name;

    @PrimaryKeyColumn(type=PrimaryKeyType.CLUSTERED)
    private String releaseDate;

    private List<String> directors;
    private List<String> actors;
    private String description;
    private String encodedPoster;
    private float itemCount;

    // List of comments. For the purposes of this app, comments on content cannot have any replies. Not trying to emulate a discussion based service

    public MovieEntity(String name, List<String> directors, List<String> actors, String description, String releaseDate, String encodedPoster) {
        this.name = name;
        this.directors = directors;
        this.actors = actors;
        this.description = description;
        this.releaseDate = releaseDate;
        this.encodedPoster = encodedPoster;
    }

}
