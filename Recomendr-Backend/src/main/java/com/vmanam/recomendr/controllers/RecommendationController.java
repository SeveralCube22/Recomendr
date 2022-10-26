package com.vmanam.recomendr.controllers;

import com.vmanam.recomendr.entities.ItemSimilarityEntity;
import com.vmanam.recomendr.entities.MovieEntity;
import com.vmanam.recomendr.entities.UserItemEntity;
import com.vmanam.recomendr.repositories.ItemSimilarityRepository;
import com.vmanam.recomendr.repositories.MovieRepository;
import com.vmanam.recomendr.repositories.UserItemRepository;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api/")
public class RecommendationController {

    @Autowired
    private MovieRepository movieRepo;
    @Autowired
    private UserItemRepository userItemRepo;

    @Autowired
    private ItemSimilarityRepository similarityRepo;

    @Value("${recomendr.recomendation.k}")
    private int k;

    @GetMapping("/recommendations")
    public List<MovieRecommendationDTO> getRecommendations(@RequestParam String userId) { // for now, not caring about authentication
        Iterable<UserItemEntity> ri = userItemRepo.findByUserId(userId);
        Iterable<MovieEntity> mi = movieRepo.findAll();

        HashMap<MovieDTO, Float> rated = new HashMap<>();
        for(UserItemEntity entity : ri) {
            MovieDTO dto = new MovieDTO();
            dto.setName(entity.getMovieName());
            dto.setReleaseDate(entity.getReleaseDate());
            rated.put(dto, entity.getRating());
        }

        List<MovieDTO> movies = new ArrayList<>();
        for(MovieEntity movie : mi) {
            MovieDTO dto = new MovieDTO();
            dto.setName(movie.getName());
            dto.setReleaseDate(movie.getReleaseDate());
            movies.add(dto);
        }

        List<MovieDTO> unseen = new ArrayList<>();
        for(MovieDTO movie : movies) {
            if(!rated.containsKey(movie))
                unseen.add(movie);
        }

        List<MovieRecommendationDTO> recommendations = new ArrayList<>();

        for(MovieDTO movie : unseen) {
            List<ItemSimilarityEntity> similarity = new ArrayList<>();
            for(MovieDTO ratedMovie : rated.keySet()) {
                Optional<ItemSimilarityEntity> result = similarityRepo.findItemSimilarityEntityByCurrentMovieNameAndCurrentMovieReleaseDateAndOtherMovieNameAndOtherMovieReleaseDate(
                        movie.getName(), movie.getReleaseDate(),
                        ratedMovie.getName(), ratedMovie.getReleaseDate() // putting rated movie in other
                );
                if(result.isPresent()) {
                    similarity.add(result.get());
                }
            }

            Collections.sort(similarity, Collections.reverseOrder(new Comparator<ItemSimilarityEntity>() {
                @Override
                public int compare(ItemSimilarityEntity s1, ItemSimilarityEntity s2) {
                   Double s1R = s1.getSimilarity();
                   Double s2R = s2.getSimilarity();
                   return s1R.compareTo(s2R);
                }
            }));

            double similarities = 0;
            double ratedSim = 0;
            for(int i = 0; i < Math.min(k, similarity.size()); i++) {
                ItemSimilarityEntity simE = similarity.get(i);
                MovieDTO iDTO = new MovieDTO();
                iDTO.setName(simE.getOtherMovieName()); // set rated movie in other
                iDTO.setReleaseDate(simE.getOtherMovieReleaseDate());
                Float item = rated.get(iDTO);
                if(item != null) {
                    ratedSim += simE.getSimilarity() * item;
                    similarities += simE.getSimilarity();
                }
            }
            MovieRecommendationDTO dto = new MovieRecommendationDTO();
            dto.setMovie(movie.getName());
            dto.setReleaseDate(movie.getReleaseDate());
            dto.setRating(ratedSim / similarities);
            recommendations.add(dto);
        }

        Collections.sort(recommendations, Collections.reverseOrder( new Comparator<MovieRecommendationDTO>() {
            @Override
            public int compare(MovieRecommendationDTO s1, MovieRecommendationDTO s2) {
                Double s1R = s1.getRating();
                Double s2R = s2.getRating();
                return s1R.compareTo(s2R);
            }
        }));

        return recommendations;
    }
}
