package com.example.android.popularmovies2;


import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


import com.example.android.popularmovies2.RepositoryUtilities.FilmInfo;
import com.example.android.popularmovies2.RepositoryUtilities.FilmType;
import com.example.android.popularmovies2.database.Film;

import java.util.List;

public class DetailsViewModel extends AndroidViewModel {
    private FilmRepository filmRepository;
    private Film currentFilm;
    private LiveData<List<Film>> favourites;
    private List<Film> favouritesList;
    private boolean filmIsFavourite;
    private MutableLiveData<FilmInfo> mfilmInfo;
    private FilmInfo mCurrentFilmInfo;

    public DetailsViewModel(@NonNull Application application) {
        super(application);
        filmRepository = FilmRepository.getFilmRepository(application);
        favourites = filmRepository.getFavourites();
        setFavouritesObserver();
        mfilmInfo = filmRepository.getFilmInfo();
        setFilmInfoObserver();
    }

    private void setFilmInfoObserver() {
        mfilmInfo.observeForever(new Observer<FilmInfo>() {
            @Override
            public void onChanged(@Nullable FilmInfo filmInfo) {
                mCurrentFilmInfo = filmInfo;
            }
        });
    }

    // getters and setters
    public FilmInfo getCurrentFilmInfo() {
        return mCurrentFilmInfo;
    }

    public MutableLiveData<FilmInfo> getFilmInfo() {
        return mfilmInfo;
    }

    public void setId(Film currentFilm) {
        for (int i = 0; i < favouritesList.size(); i++) {
            if (favouritesList.get(i).getTitle().equals(currentFilm.getTitle())) {
                currentFilm.setId(favouritesList.get(i).getId());
            }
        }
    }

    public void setFilmIsFavourite(boolean toggle) {
        filmIsFavourite = toggle;

    }

    public boolean getFilmIsFavourite() {
        return filmIsFavourite;

    }


    public void setCurrentFilm(Film film) {
        currentFilm = film;

    }


    // updating methods
    public void updateFilmInfo(int movieId) {
        filmRepository.updateFilmInfo(movieId);
    }

    public void updateFavourites() {
        if (!filmIsFavourite && favouritesChecker(currentFilm.getTitle())) {
            setId(currentFilm);

            filmRepository.deleteFilm(currentFilm);

        } else if (filmIsFavourite && !favouritesChecker(currentFilm.getTitle())) {
            currentFilm.setFilmType(FilmType.FAVOURITE);
            currentFilm.setIconGraphicId(R.drawable.svg_small_heart_pink);
            filmRepository.insertFilm(currentFilm);
        }
    }

    // utilities
    public boolean favouritesChecker(String title) {
        for (int i = 0; i < favouritesList.size(); i++) {
            if (favouritesList.get(i).getTitle().equals(title)) {
                return true;
            }
        }
        return false;
    }

    private void setFavouritesObserver() {
        favourites.observeForever(new Observer<List<Film>>() {
            @Override
            public void onChanged(@Nullable List<Film> films) {
                favouritesList = films;
            }
        });
    }


}

