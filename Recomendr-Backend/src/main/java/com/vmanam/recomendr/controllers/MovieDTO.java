package com.vmanam.recomendr.controllers;

import lombok.Data;

import java.awt.*;
import java.util.List;

@Data
public class MovieDTO {
    private String name;
    private List<String> directors;
    private List<String> actors;
    private String description;
    private String releaseDate;
    private String encodedPoster;
}
