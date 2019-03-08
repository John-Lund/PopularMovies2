package com.example.android.popularmovies2;


import android.annotation.SuppressLint;
import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;



import com.example.android.popularmovies2.RepositoryUtilities.FilmInfo;
import com.example.android.popularmovies2.RepositoryUtilities.FilmReview;
import com.example.android.popularmovies2.RepositoryUtilities.FilmType;
import com.example.android.popularmovies2.RepositoryUtilities.Utilities;
import com.example.android.popularmovies2.RepositoryUtilities.VideoLink;
import com.example.android.popularmovies2.database.Film;
import com.example.android.popularmovies2.database.FilmDao;
import com.example.android.popularmovies2.database.FilmDatabase;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;


public class FilmRepository {
    // todo: this is where you need to insert your API key
    private static final String TOP_RATED_MOVIES_URL = "https://api.themoviedb.org/3/movie/top_rated?[insert API key here]&language=en-US&page=1";
    private static final String POPULAR_MOVIES_URL = "https://api.themoviedb.org/3/movie/popular?[insert API key here]&language=en-US&page=1";
    private static final String LINKS_BASE_URL = "https://api.themoviedb.org/3/movie/";
    private static final String VIDEO_LINKS_END_URL = "/videos?[insert API key here]&language=en-US";
    private static final String REVIEW_LINKS_END_URL = "/reviews?[insert API key here]";
    private static FilmRepository INSTANCE;
    private final ReentrantLock mLock;
    private FilmDao filmDao;
    private LiveData<List<Film>> favourites;
    private MutableLiveData<List<Film>> topRatedFilms = new MutableLiveData<>();
    private MutableLiveData<List<Film>> mostPopularFilms = new MutableLiveData<>();
    private Utilities mUtilities;
    private ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
    private MutableLiveData<FilmInfo> mFilmInfo = new MutableLiveData<>();

    private FilmRepository(Application application) {
        FilmDatabase db = FilmDatabase.getDatabase(application);
        filmDao = db.filmDao();
        mUtilities = Utilities.getsInstance();
        favourites = filmDao.getFavourites();
        mLock = new ReentrantLock();
        new loadFilmData(TOP_RATED_MOVIES_URL, FilmType.HIGHLY_RATED).execute();
        new loadFilmData(POPULAR_MOVIES_URL, FilmType.POPULAR).execute();

    }

    public static FilmRepository getFilmRepository(Application application) {
        if (INSTANCE == null) {
            synchronized (FilmRepository.class) {
                INSTANCE = new FilmRepository(application);
            }
        }
        return INSTANCE;
    }


    public void updateFilmInfo(int movieId) {
        new GetFilmInfoObject(movieId).execute();
    }

    public MutableLiveData<FilmInfo> getFilmInfo() {
        return mFilmInfo;
    }

    public void refreshOnlineData() {
        new loadFilmData(TOP_RATED_MOVIES_URL, FilmType.HIGHLY_RATED).execute();
        new loadFilmData(POPULAR_MOVIES_URL, FilmType.POPULAR).execute();
    }

    public MutableLiveData<List<Film>> getMostPopularFilms() {
        return mostPopularFilms;
    }

    public MutableLiveData<List<Film>> getTopRatedFilms() {
        return topRatedFilms;
    }

    public LiveData<List<Film>> getFavourites() {
        return favourites;
    }

    public void insertFilm(Film film) {
        mExecutorService.execute(new InsertFilm(film));
    }

    public void deleteFilm(Film film) {
        mExecutorService.execute(new DeleteFilm(film));
    }

    @SuppressLint("StaticFieldLeak")
    private class GetFilmInfoObject extends AsyncTask<Void, Void, FilmInfo> {
        private String videoLinksUrlString;
        private String reviewLinksUrlString;
        private int movieId;

        private GetFilmInfoObject(int movieId) {
            this.movieId = movieId;
            StringBuilder sb = new StringBuilder();
            sb.append(LINKS_BASE_URL).append(String.valueOf(movieId)).append(VIDEO_LINKS_END_URL);
            this.videoLinksUrlString = sb.toString();
            sb.setLength(0);
            sb.append(LINKS_BASE_URL).append(String.valueOf(movieId)).append(REVIEW_LINKS_END_URL);
            this.reviewLinksUrlString = sb.toString();
        }

        @Override
        protected FilmInfo doInBackground(Void... voids) {
            List<VideoLink> videoLinks;
            List<FilmReview> filmReviews;
            while (true) {

                if (mLock.tryLock()) {
                    try {
                        URL url = mUtilities.createUrl(videoLinksUrlString);

                        String jsonResponse = "";
                        try {
                            jsonResponse = mUtilities.loadJSON(url);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        videoLinks = mUtilities.extractVideoLinks(jsonResponse);
                    } finally {
                        mLock.unlock();
                    }
                    break;
                }
            }

            while (true) {

                if (mLock.tryLock()) {
                    try {
                        URL url = mUtilities.createUrl(reviewLinksUrlString);

                        String jsonResponse = "";
                        try {
                            jsonResponse = mUtilities.loadJSON(url);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        filmReviews = mUtilities.extractReviews(jsonResponse);
                    } finally {
                        mLock.unlock();
                    }
                    break;
                }
            }
            return new FilmInfo(filmReviews, videoLinks, movieId);
        }

        @Override
        protected void onPostExecute(FilmInfo filmInfo) {
            mFilmInfo.setValue(filmInfo);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class loadFilmData extends AsyncTask<Void, Void, List<Film>> {
        private String urlString;
        private FilmType filmType;

        private loadFilmData(String url, FilmType filmType) {
            this.urlString = url;
            this.filmType = filmType;
        }

        @Override
        protected List<Film> doInBackground(Void... voids) {
            List<Film> films;
            while (true) {

                if (mLock.tryLock()) {

                    try {
                        URL url = mUtilities.createUrl(urlString);

                        String jsonResponse = "";
                        try {
                            jsonResponse = mUtilities.loadJSON(url);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        films = mUtilities.extractFilmObjects(jsonResponse, filmType);
                    } finally {
                        mLock.unlock();
                    }
                    break;
                }
            }

            return films;
        }

        @Override
        protected void onPostExecute(List<Film> films) {
            if (urlString.equals(POPULAR_MOVIES_URL)) {
                mostPopularFilms.setValue(films);
            } else {
                topRatedFilms.setValue(films);
            }
        }
    }

    private class InsertFilm implements Runnable {
        private Film film;

        private InsertFilm(Film film) {
            this.film = film;
        }

        @Override
        public void run() {
            filmDao.insertFilm(film);
        }
    }

    private class DeleteFilm implements Runnable {
        private Film film;

        private DeleteFilm(Film film) {
            this.film = film;

        }

        @Override
        public void run() {
            filmDao.deleteFilm(film);
        }
    }


}




















