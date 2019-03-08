package com.example.android.popularmovies2.RepositoryUtilities;

public class FilmReview {
    private String authorName;
    private String reviewText;

    public FilmReview(String authorName, String reviewText){
        this.authorName = authorName;
        this.reviewText = reviewText;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getReviewText() {
        return reviewText;
    }
}
