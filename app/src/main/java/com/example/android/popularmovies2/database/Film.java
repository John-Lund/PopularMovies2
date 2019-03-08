package com.example.android.popularmovies2.database;


import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;


import com.example.android.popularmovies2.RepositoryUtilities.FilmType;

@Entity(tableName = "films_table")
public class Film implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private String releaseDate;
    private String voteAverage;
    private String plotSynopsis;
    private FilmType filmType;
    private int iconGraphicId;
    private String thumbnailUrl;
    private String hdUrl;
    private int movieId;

    @Ignore
    public Film(String title, String releaseDate, String voteAverage, String plotSynopsis, FilmType filmType, int iconGraphicId, String thumbnailUrl, String hdUrl, int movieId) {
        this.title = title;
        this.releaseDate = releaseDate;
        this.voteAverage = voteAverage;
        this.plotSynopsis = plotSynopsis;
        this.filmType = filmType;
        this.iconGraphicId = iconGraphicId;
        this.thumbnailUrl = thumbnailUrl;
        this.hdUrl = hdUrl;
        this.movieId = movieId;
    }

    public Film() {
    }

    public Film(int id, String title, String releaseDate, String voteAverage, String plotSynopsis, FilmType filmType, int iconGraphicId, String thumbnailUrl, String hdUrl, int movieId) {
        this.id = id;
        this.title = title;
        this.releaseDate = releaseDate;
        this.voteAverage = voteAverage;
        this.plotSynopsis = plotSynopsis;
        this.filmType = filmType;
        this.iconGraphicId = iconGraphicId;
        this.thumbnailUrl = thumbnailUrl;
        this.hdUrl = hdUrl;
        this.movieId = movieId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(String voteAverage) {
        this.voteAverage = voteAverage;
    }

    public String getPlotSynopsis() {
        return plotSynopsis;
    }

    public void setPlotSynopsis(String plotSynopsis) {
        this.plotSynopsis = plotSynopsis;
    }

    public FilmType getFilmType() {
        return filmType;
    }

    public void setFilmType(FilmType filmType) {
        this.filmType = filmType;
    }

    public int getIconGraphicId() {
        return iconGraphicId;
    }

    public void setIconGraphicId(int iconGraphicId) {
        this.iconGraphicId = iconGraphicId;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getHdUrl() {
        return hdUrl;
    }

    public void setHdUrl(String hdUrl) {
        this.hdUrl = hdUrl;
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.title);
        dest.writeString(this.releaseDate);
        dest.writeString(this.voteAverage);
        dest.writeString(this.plotSynopsis);
        dest.writeInt(this.filmType == null ? -1 : this.filmType.ordinal());
        dest.writeInt(this.iconGraphicId);
        dest.writeString(this.thumbnailUrl);
        dest.writeString(this.hdUrl);
        dest.writeInt(this.movieId);
    }

    protected Film(Parcel in) {
        this.id = in.readInt();
        this.title = in.readString();
        this.releaseDate = in.readString();
        this.voteAverage = in.readString();
        this.plotSynopsis = in.readString();
        int tmpFilmType = in.readInt();
        this.filmType = tmpFilmType == -1 ? null : FilmType.values()[tmpFilmType];
        this.iconGraphicId = in.readInt();
        this.thumbnailUrl = in.readString();
        this.hdUrl = in.readString();
        this.movieId = in.readInt();
    }

    public static final Parcelable.Creator<Film> CREATOR = new Parcelable.Creator<Film>() {
        @Override
        public Film createFromParcel(Parcel source) {
            return new Film(source);
        }

        @Override
        public Film[] newArray(int size) {
            return new Film[size];
        }
    };
}
