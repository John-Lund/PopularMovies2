package com.example.android.popularmovies2.RepositoryUtilities;


import com.example.android.popularmovies2.Constants;
import com.example.android.popularmovies2.database.Film;
import com.example.android.popularmovies2.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class Utilities {

    private static Utilities sInstance;
    private static String THUMBNAIL_URL_BASE = "https://img.youtube.com/vi/";
    private static String THUMBNAIL_URL_END = "/hqdefault.jpg";
    private static String VIDEO_URL_BASE = "https://www.youtube.com/watch?v=";

    private Utilities() {
    }

    public static Utilities getsInstance() {

        if (sInstance == null) {
            synchronized (Utilities.class) {
                sInstance = new Utilities();
            }
        }
        return sInstance;
    }

    public List<FilmReview> extractReviews(String jsonResponse) {
        StringBuilder sb = new StringBuilder();
        ArrayList<FilmReview> filmReviews = new ArrayList<>();
        try {
            JSONObject jsonRootObject = new JSONObject(jsonResponse);
            JSONArray jsonArray = jsonRootObject.getJSONArray("results");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.has("author")
                        && jsonObject.has("content")) {
                    FilmReview filmReview = new FilmReview(jsonObject.optString("author", "Mr Normal"), jsonObject.optString("content", "It was boring."));
                    filmReviews.add(filmReview);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return filmReviews;
    }

    public List<VideoLink> extractVideoLinks(String jsonResponse) {
        StringBuilder sb = new StringBuilder();
        ArrayList<VideoLink> videoLinks = new ArrayList<>();
        try {
            JSONObject jsonRootObject = new JSONObject(jsonResponse);
            JSONArray jsonArray = jsonRootObject.getJSONArray("results");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.optString("site", null).equals("YouTube")) {
                    String key = jsonObject.optString("key", null);
                    if (key != null) {
                        sb.append(THUMBNAIL_URL_BASE).append(key).append(THUMBNAIL_URL_END);
                        String thumbnailUrlString = sb.toString();
                        sb.setLength(0);
                        sb.append(VIDEO_URL_BASE).append(key);
                        String videoUrlString = sb.toString();
                        sb.setLength(0);
                        VideoLink videoLink = new VideoLink(thumbnailUrlString, videoUrlString);
                        videoLinks.add(videoLink);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return videoLinks;
    }

    // method to extract film objects from JSON string
    public List<Film> extractFilmObjects(String jsonResponse, FilmType filmType) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ArrayList<Film> films = new ArrayList<>();
        try {
            JSONObject jsonRootObject = new JSONObject(jsonResponse);
            JSONArray jsonArray = jsonRootObject.getJSONArray("results");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String title = jsonObject.optString("title", "Title unknown");
                String releaseDate = jsonObject.optString("release_date", "Release date unknown");
                String moviePosterUrlFragment = jsonObject.optString("poster_path");
                double voteAverage = jsonObject.optDouble("vote_average", 0.0d);
                String voteAverageText = String.valueOf(voteAverage);
                String plotSynopsis = jsonObject.optString("overview", "No synopsis available");
                int movieId = jsonObject.optInt("id", 0);
                int iconId;
                if (filmType == FilmType.HIGHLY_RATED) {
                    iconId = getStarGraphic(voteAverage);
                } else {
                    iconId = R.drawable.svg_thumb_up_red_small;
                }
                StringBuilder sb = new StringBuilder();
                sb.append(Constants.POSTER_HD_URL_BASE).append(moviePosterUrlFragment);
                String hdUrl = sb.toString();
                sb.setLength(0);
                sb.append(Constants.POSTER_URL_BASE).append(moviePosterUrlFragment);
                String thumbnailUrl = sb.toString();
                Film film = new Film(title, releaseDate, voteAverageText, plotSynopsis, filmType, iconId, thumbnailUrl, hdUrl, movieId);
                films.add(film);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return films;
    }


    // method to convert url string to URL
    public URL createUrl(String urlString) {
        URL url;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
        return url;
    }

    // method to load JSON string
    public String loadJSON(URL url) throws IOException {
        String jsonResponse = "";
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        if (url == null) {
            return null;
        }
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.connect();
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = convertStream(inputStream);
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    // method to convert input stream to string
    private String convertStream(InputStream inputStream) throws IOException {
        StringBuilder builder = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader streamReader = new InputStreamReader(inputStream);
            BufferedReader reader = new BufferedReader(streamReader);
            String output = reader.readLine();
            while (output != null) {
                builder.append(output);
                output = reader.readLine();
            }
        }
        return builder.toString();
    }

    // method to get correct graphic resource according to film rating
    public static int getStarGraphic(double rating) {
        int roundRating = (int) Math.round(rating);
        switch (roundRating) {
            case 0:
                return R.drawable.svg_stars_row_none;
            case 1:
                return R.drawable.svg_stars_row_half;
            case 2:
                return R.drawable.svg_stars_row_one;
            case 3:
                return R.drawable.svg_stars_row_one_half;
            case 4:
                return R.drawable.svg_stars_row_two;
            case 5:
                return R.drawable.svg_stars_row_two_half;
            case 6:
                return R.drawable.svg_stars_row_three;
            case 7:
                return R.drawable.svg_stars_row_three_half;
            case 8:
                return R.drawable.svg_stars_row_four;
            case 9:
                return R.drawable.svg_stars_row_four_half;
            default:
                return R.drawable.svg_stars_row_five;
        }
    }
}


