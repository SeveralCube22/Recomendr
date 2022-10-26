package com.vmanam.recomendr.controllers;

import lombok.Data;

@Data
public class MovieRecommendationDTO {
    private String movie;
    private String releaseDate;
    private double rating;
}
