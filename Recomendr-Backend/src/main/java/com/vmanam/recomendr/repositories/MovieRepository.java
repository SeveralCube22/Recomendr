package com.vmanam.recomendr.repositories;

import com.vmanam.recomendr.entities.MovieEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface MovieRepository extends CrudRepository<MovieEntity, String> {
    Optional<MovieEntity> findByNameAndReleaseDate(String name, String releaseDate);
}
