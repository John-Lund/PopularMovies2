package com.example.android.popularmovies2.database;

import android.arch.persistence.room.TypeConverter;


import com.example.android.popularmovies2.RepositoryUtilities.FilmType;

import static com.example.android.popularmovies2.RepositoryUtilities.FilmType.FAVOURITE;
import static com.example.android.popularmovies2.RepositoryUtilities.FilmType.HIGHLY_RATED;
import static com.example.android.popularmovies2.RepositoryUtilities.FilmType.POPULAR;

public class FilmTypeConverter {

    @TypeConverter
    public static FilmType toFilmType(int type) {
        switch (type) {
            case 1:
                return POPULAR;
            case 2:
                return HIGHLY_RATED;
            default:
                return FAVOURITE;
        }
    }

    @TypeConverter
    public static int toInt(FilmType filmType) {
        switch (filmType) {
            case POPULAR:
                return 1;
            case HIGHLY_RATED:
                return 2;
            default:
                return 3;
        }
    }
}