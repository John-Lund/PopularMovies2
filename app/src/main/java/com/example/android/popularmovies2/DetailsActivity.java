package com.example.android.popularmovies2;

import android.annotation.SuppressLint;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.popularmovies2.RepositoryUtilities.FilmInfo;
import com.example.android.popularmovies2.RepositoryUtilities.FilmReview;
import com.example.android.popularmovies2.RepositoryUtilities.Utilities;
import com.example.android.popularmovies2.RepositoryUtilities.VideoLink;
import com.example.android.popularmovies2.database.Film;
import com.example.android.popularmovies2.databinding.ActivityDetailsBinding;

import com.squareup.picasso.Picasso;

import java.util.List;


public class DetailsActivity extends AppCompatActivity {
    private Film film;
    private ActivityDetailsBinding mBindingdetails;
    private DetailsViewModel viewModel;
    private VideoAdapter mAdapter;
    private GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 1);

    // setting UI to full screen
    @Override
    protected void onResume() {
        super.onResume();
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    // saving value of filmIsFavourite to reserve user selection from favourite button
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (viewModel.getFilmIsFavourite()) {
            outState.putString(Constants.FILM_TYPE, Constants.FAVOURITE);
        } else {
            outState.putString(Constants.FILM_TYPE, Constants.NONFAVOURITE);
        }
        super.onSaveInstanceState(outState);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBindingdetails = DataBindingUtil.setContentView(this, R.layout.activity_details);
        film = getIntent().getParcelableExtra(Constants.FILM_OBJECT);
        viewModel = ViewModelProviders.of(this).get(DetailsViewModel.class);
        // resetting filmIsFavourite if activity has been destroyed
        if (savedInstanceState != null
                && savedInstanceState.containsKey(Constants.FILM_TYPE)){
            if (savedInstanceState.getString(Constants.FILM_TYPE, Constants.NONFAVOURITE).equals(Constants.FAVOURITE)) {
                viewModel.setFilmIsFavourite(true);
            } else {
                viewModel.setFilmIsFavourite(false);
            }
        } else {
            viewModel.setFilmIsFavourite(viewModel.favouritesChecker(film.getTitle()));
        }
        viewModel.setCurrentFilm(film);
        // setting up listeners
        setUpListeners();
        // loading film object data into UI
        setUpUiFromFilmObject();
        // setting up the recycler view for the video thumbnails display
        setVideoAdapter();
        // loading filmInfo object and giving review and video lists to correct methods
        setData();
    }
    // loading filmInfo object and giving review and video lists to correct methods
    private void setData() {
        FilmInfo filmInfo = viewModel.getCurrentFilmInfo();
        if (filmInfo != null && filmInfo.getMovieId() == film.getMovieId()
                && filmInfo.getVideoLinks().size() != 0 && filmInfo.getReviews().size() != 0) {
            setAdapterData(filmInfo);
            createReviews(filmInfo.getReviews());
        } else {
            viewModel.updateFilmInfo(film.getMovieId());
            viewModel.getFilmInfo().observe(this, new Observer<FilmInfo>() {
                @Override
                public void onChanged(@Nullable FilmInfo filmInfo) {
                    if (filmInfo != null && filmInfo.getMovieId() == film.getMovieId()) {
                        setAdapterData(filmInfo);
                        createReviews(filmInfo.getReviews());
                    }
                }
            });
        }
    }

    // updating FilmAdapter data
    private void setAdapterData(FilmInfo filmInfo) {
        if (filmInfo.getVideoLinks() != null
                && filmInfo.getVideoLinks().size() > 0) {
            mAdapter.setDataList(viewModel.getCurrentFilmInfo().getVideoLinks());
            mBindingdetails.noVideosTextview.setVisibility(View.GONE);
        }
    }

    // creating views for reviews, populating them with review data and adding them to the UI
    private void createReviews(List<FilmReview> reviews) {
        if (reviews != null && reviews.size() > 0) {
            mBindingdetails.noReviewsTextview.setVisibility(View.GONE);
            for (int i = 0; i < reviews.size(); i++) {
                FilmReview filmReview = reviews.get(i);
                View reviewWrapper = LayoutInflater.from(this)
                        .inflate(R.layout.review_wrapper_layout, mBindingdetails.detailsReviewLayout, false);
                TextView author = reviewWrapper.findViewById(R.id.author_text_view);
                TextView reviewText = reviewWrapper.findViewById(R.id.review_text_view);
                author.setText(filmReview.getAuthorName());
                reviewText.setText(filmReview.getReviewText());
                mBindingdetails.detailsReviewLayout.addView(reviewWrapper);
            }
        }
    }
    // setting up the adapter for the video thumbnails with links to relevant YouTube videos
    private void setVideoAdapter() {
        View.OnClickListener itemListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkInternet()) {
                    Toast.makeText(DetailsActivity.this, R.string.video_warning, Toast.LENGTH_SHORT).show();
                    return;
                }
                VideoLink videoLink = (VideoLink) v.getTag();
                Uri webpage = Uri.parse(videoLink.getVideoLinkString());
                Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        };
        gridLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mBindingdetails.videosRecycler.setLayoutManager(gridLayoutManager);
        mAdapter = new VideoAdapter(itemListener);
        mBindingdetails.videosRecycler.setAdapter(mAdapter);
    }
    // loading film object data into UI
    private void setUpUiFromFilmObject() {
        mBindingdetails.detailsTitleTextView.setText(film.getTitle());
        mBindingdetails.detailsDateTextView.setText(film.getReleaseDate());
        Picasso.with(this)
                .load(film.getThumbnailUrl())
                .error(R.drawable.movieapp_image_placeholder_200)
                .into(mBindingdetails.detailsThumbnail);
        Picasso.with(this)
                .load(film.getHdUrl())
                .error(R.drawable.movieapp_image_placeholder_500)
                .into(mBindingdetails.hdPosterImageView);
        mBindingdetails.detailsRating.setText(String.valueOf(film.getVoteAverage()));
        mBindingdetails.detailsSysnopsisTextView.setText(film.getPlotSynopsis());
        mBindingdetails.detailsStarsImageView
                .setImageResource(Utilities.getStarGraphic(Double.valueOf(film.getVoteAverage())));
    }

    // updating favourites table if user presses back button
    @Override
    public void onBackPressed() {
        viewModel.updateFavourites();
        super.onBackPressed();
    }
    // setting up listeners
    @SuppressLint("ClickableViewAccessibility")
    private void setUpListeners() {
        if (viewModel.getFilmIsFavourite()) {
            mBindingdetails.detailsFavouriteButton.setBackgroundResource(R.drawable.svg_heart_pink_circle_info);
        }
        View.OnClickListener menuListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                switch (id) {
                    case R.id.details_back_button:
                        viewModel.updateFavourites();
                        finish();
                        break;
                    case R.id.details_info_button:
                        mBindingdetails.detailsInfoScrim.setVisibility(View.VISIBLE);
                        mBindingdetails.detailsInfoMessageImageview.setVisibility(View.VISIBLE);
                        break;
                    case R.id.details_favourite_button:
                        if (!viewModel.getFilmIsFavourite()) {
                            mBindingdetails.detailsFavouriteButton.setBackgroundResource(R.drawable.svg_heart_pink_circle_info);
                            viewModel.setFilmIsFavourite(true);
                        } else {
                            mBindingdetails.detailsFavouriteButton.setBackgroundResource(R.drawable.svg_heart_blue_circle_info);
                            viewModel.setFilmIsFavourite(false);
                        }
                }
            }
        };
        mBindingdetails.detailsFavouriteButton.setOnClickListener(menuListener);
        mBindingdetails.detailsBackButton.setOnClickListener(menuListener);
        mBindingdetails.detailsInfoButton.setOnClickListener(menuListener);
        mBindingdetails.detailsInfoScrim.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mBindingdetails.detailsInfoScrim.setVisibility(View.GONE);
                mBindingdetails.detailsInfoMessageImageview.setVisibility(View.GONE);
                return true;
            }
        });
    }

    // method to check internet connection
    private boolean checkInternet() {
        ConnectivityManager manager =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork;
        activeNetwork = (manager != null) ? manager.getActiveNetworkInfo() : null;
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }
}


