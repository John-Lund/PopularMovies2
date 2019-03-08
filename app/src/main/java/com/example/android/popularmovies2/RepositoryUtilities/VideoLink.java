package com.example.android.popularmovies2.RepositoryUtilities;

public class VideoLink {

    private String thumbnailUrlString;
    private String videoLinkString;

    public VideoLink(String thumbnailUrlString, String videoLinkString) {
        this.thumbnailUrlString = thumbnailUrlString;
        this.videoLinkString = videoLinkString;
    }

    public String getThumbnailUrlString() {
        return thumbnailUrlString;
    }

    public String getVideoLinkString() {
        return videoLinkString;
    }
}