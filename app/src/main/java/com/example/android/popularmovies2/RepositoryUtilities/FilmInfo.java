package com.example.android.popularmovies2.RepositoryUtilities;

import java.util.List;

public class FilmInfo {
    private int movieId;
    private List<FilmReview> reviews;
    private List<VideoLink> videoLinks;

    public FilmInfo(List<FilmReview> reviews, List<VideoLink> videoLinks, int movieId) {
        this.reviews = reviews;
        this.videoLinks = videoLinks;
        this.movieId = movieId;
    }

    public List<FilmReview> getReviews() {
        return reviews;
    }

    public List<VideoLink> getVideoLinks() {
        return videoLinks;
    }

    public int getMovieId() {
        return movieId;
    }
}
