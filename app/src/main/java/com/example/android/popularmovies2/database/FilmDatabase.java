package com.example.android.popularmovies2.database;



import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;



@Database(entities = {Film.class}, version = 1, exportSchema = false)
@TypeConverters(FilmTypeConverter.class)

public abstract class FilmDatabase extends RoomDatabase {
    public abstract FilmDao filmDao();
    private static FilmDatabase INSTANCE;

    public static FilmDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (FilmDatabase.class) {
                if (INSTANCE == null) {

                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            FilmDatabase.class, "film_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}