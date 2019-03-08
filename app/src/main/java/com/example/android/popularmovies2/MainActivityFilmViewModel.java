package com.example.android.popularmovies2;


import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;



import com.example.android.popularmovies2.database.Film;

import java.util.List;

public class MainActivityFilmViewModel extends AndroidViewModel {
    private FilmRepository filmRepository;
    private LiveData<List<Film>> favourites;
    private MutableLiveData<List<Film>> topRated;
    private MutableLiveData<List<Film>> mostPopular;
    private List<Film> ratedList;
    private List<Film> popularList;
    private List<Film> favouritesList;

    public MainActivityFilmViewModel(@NonNull Application application) {
        super(application);
        filmRepository = FilmRepository.getFilmRepository(application);
        favourites = filmRepository.getFavourites();
        topRated = filmRepository.getTopRatedFilms();
        mostPopular = filmRepository.getMostPopularFilms();
        setMutableObservers();
        setFavouritesObserver();
    }

    public void refreshOnlineData() {
        filmRepository.refreshOnlineData();
        //   setMutableObservers();
    }

    private void setFavouritesObserver() {
        favourites.observeForever(new Observer<List<Film>>() {
            @Override
            public void onChanged(@Nullable List<Film> films) {
                favouritesList = films;
            }
        });
    }

    private void setMutableObservers() {
        topRated.observeForever(new Observer<List<Film>>() {
            @Override
            public void onChanged(@Nullable List<Film> films) {
                ratedList = films;
            }
        });
        mostPopular.observeForever(new Observer<List<Film>>() {
            @Override
            public void onChanged(@Nullable List<Film> films) {
                popularList = films;
            }
        });
    }

    ///// List getters
    public List<Film> getRatedList() {
        return ratedList;
    }

    public List<Film> getPopularList() {
        return popularList;
    }

    public List<Film> getFavouritesList() {
        return favouritesList;
    }

    /// LiveData Getters/////////////
    public LiveData<List<Film>> getFavourites() {
        return favourites;
    }

    public MutableLiveData<List<Film>> getTopRated() {
        return topRated;
    }

    public MutableLiveData<List<Film>> getMostPopular() {
        return mostPopular;
    }
}
