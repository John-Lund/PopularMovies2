package com.example.android.popularmovies2.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface FilmDao {


    @Query("SELECT * FROM films_table ORDER BY title ASC")
    LiveData<List<Film>> getFavourites();

    @Insert
    void insertFilm(Film film);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateFilm(Film film);

    @Delete
    void deleteFilm(Film film);

    @Query("SELECT * FROM films_table WHERE id = :id")
    LiveData<Film> loadFilmById(int id);





}
