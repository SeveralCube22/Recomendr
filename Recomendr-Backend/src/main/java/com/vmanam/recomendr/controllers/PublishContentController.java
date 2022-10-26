package com.vmanam.recomendr.controllers;

import com.vmanam.recomendr.entities.MovieEntity;
import com.vmanam.recomendr.repositories.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/publish/")
public class PublishContentController {
    @Autowired
    MovieRepository movieRepo;

    @PostMapping("/movie")
    public ResponseEntity postMovie(@RequestBody MovieDTO movie) {
        MovieEntity res = movieRepo.findByNameAndReleaseDate(movie.getName(), movie.getReleaseDate()).orElse(null);
        if(res == null) {
            movieRepo.save(new MovieEntity(movie.getName(), movie.getDirectors(), movie.getActors(), movie.getDescription(), movie.getReleaseDate(), movie.getEncodedPoster()));
            return new ResponseEntity<>(HttpStatus.CREATED);
        }
        else
            return ResponseEntity.badRequest().body("Movie Already Exits");
    }
}
