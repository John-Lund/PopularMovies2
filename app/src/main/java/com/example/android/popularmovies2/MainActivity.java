package com.example.android.popularmovies2;


import android.annotation.SuppressLint;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.android.popularmovies2.RepositoryUtilities.ConnectivityReceiver;
import com.example.android.popularmovies2.database.Film;
import com.example.android.popularmovies2.databinding.ActivityMainBinding;


import java.util.List;


public class MainActivity extends AppCompatActivity {
    private MainActivityFilmViewModel viewModel;
    private SharedPreferences mSharedPrefs;
    private Intent mIntent;
    private FilmRecyclerAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);

    // animation fields
    private ConstraintLayout mConstraintLayout;
    private ConstraintSet mConstraintSetStart = new ConstraintSet();
    private ConstraintSet mConstraintSetStep1 = new ConstraintSet();
    private ConstraintSet mConstraintSetStep2OpenFavourites = new ConstraintSet();
    private ConstraintSet mConstraintSetStep2OpenRated = new ConstraintSet();
    private ConstraintSet mConstraintSetStep2OpenPopular = new ConstraintSet();


    private ChangeBounds mStep1OpenChangeBounds = new ChangeBounds();
    private ChangeBounds mStep1CloseChangeBounds = new ChangeBounds();
    private ChangeBounds mStep2ChangeBoundsPopular = new ChangeBounds();
    private ChangeBounds mStep2ChangeBoundsRated = new ChangeBounds();
    private ChangeBounds mStep2ChangeBoundsFavourites = new ChangeBounds();


    private int mStage1OpenTransitionSpeed = 200;
    private int mStage1CloseTransitionSpeed = 800;
    private int mStage2TransitionSpeed = 200;
    private OvershootInterpolator mOverShoot = new OvershootInterpolator();
    private boolean mClosing = false;
    private boolean mOptionsAreOpen = false;
    private boolean mAnimationIsRunning = false;
    private AccelerateInterpolator mAccelerateInterpolator = new AccelerateInterpolator(4.0f);
    private int mElegantButtonCloseId;
    private ImageButton mElegantCloseButton;
    private ActivityMainBinding mBindingMain;

    private boolean mInternetWarningWasGiven = false;
    private ConnectivityReceiver connectivityReceiver;

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter connectionFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        connectivityReceiver = new ConnectivityReceiver(getApplication(), this);
        registerReceiver(connectivityReceiver,connectionFilter );

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(connectivityReceiver);
    }

    // saving whether user was already warned about internet connection
    @Override
    protected void onSaveInstanceState(Bundle outState) {

        if (mInternetWarningWasGiven) {
            outState.putString(Constants.INTERNET_WARNING_WAS_GIVEN, Constants.INTERNET_WARNING_GIVEN_TRUE);
        } else {
            outState.putString(Constants.INTERNET_WARNING_WAS_GIVEN, Constants.INTERNET_WARNING_GIVEN_FALSE);
        }
        super.onSaveInstanceState(outState);
    }


    @SuppressLint({"ApplySharedPref", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.containsKey(Constants.INTERNET_WARNING_WAS_GIVEN)
                && savedInstanceState.getString(Constants.INTERNET_WARNING_WAS_GIVEN, Constants.INTERNET_WARNING_GIVEN_FALSE).equals(Constants.INTERNET_WARNING_GIVEN_TRUE)) {
            mInternetWarningWasGiven = true;
        }
        mBindingMain = DataBindingUtil.setContentView(this, R.layout.activity_main);

        setSupportActionBar(mBindingMain.toolbar);
        setTitle("");

        mBindingMain.floatingActionButton.setImageResource(R.drawable.white_heart);
        // creating sharedPreferences
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!mSharedPrefs.contains(Constants.FILM_LIST_TO_DISPLAY)) {
            mSharedPrefs.edit().putString(Constants.FILM_LIST_TO_DISPLAY, Constants.MOST_POPULAR).apply();
        }
        // setting FloatingActionButton appearance
        setFabAppearance(mSharedPrefs.getString(Constants.FILM_LIST_TO_DISPLAY, Constants.MOST_POPULAR));
        // setting up UI listeners
        setUpListeners();
        // animation elements
        setUpConstraintAnimationElements();

        setUpAdapter();
        setUpViewModel();

        if (!checkInternet()) {
            if (!mInternetWarningWasGiven) {

                noInternetWarning();
                mInternetWarningWasGiven = true;
            }
        }
    }

    private void userWarnings(List<Film> films, String warningType) {
        Log.e("ADAPTER FILTER", " DATA RECEIVED");
        if (films == null && warningType.equals(Constants.WARNING_TYPE_INTERNET)) {
            noInternetWarning();
        } else if (films != null && films.size() == 0 && warningType.equals(Constants.WARNING_TYPE_INTERNET)) {
            noInternetWarning();
        } else if (films == null && warningType.equals(Constants.WARNING_TYPE_FAVOURITES)) {
            noFavouritesWarning();
        } else if (films != null && films.size() == 0 && warningType.equals(Constants.WARNING_TYPE_FAVOURITES)) {
            noFavouritesWarning();
        }
        mAdapter.setDataList(films);

    }

    // method to show toast warning user they haven't created any favourites
    private void noFavouritesWarning() {
        Toast.makeText(MainActivity.this, "You haven't created any favourites.", Toast.LENGTH_LONG).show();

    }


    // method to warn user of no internet connection
    private void noInternetWarning() {
        mBindingMain.errorMessage.setText(R.string.internet_error_message);
        mBindingMain.progressBar.setVisibility(View.GONE);
        mBindingMain.noInternetScrim.setVisibility(View.VISIBLE);
        mBindingMain.noInternetWarningCardview.setVisibility(View.VISIBLE);
    }

    private void setUpConstraintAnimationElements() {
        // setting up constraint animation elements
        mConstraintLayout = findViewById(R.id.main_activity_base_layout);
        mConstraintSetStart.clone(mConstraintLayout);
        mConstraintSetStep1.clone(this, R.layout.animation_step_1);
        mConstraintSetStep2OpenPopular.clone(this, R.layout.animation_step2_popular);
        mConstraintSetStep2OpenRated.clone(this, R.layout.animation_step2_rated);
        mConstraintSetStep2OpenFavourites.clone(this, R.layout.animation_step2_favourites);



        mStep2ChangeBoundsPopular.setDuration(mStage2TransitionSpeed);
        mStep2ChangeBoundsRated.setDuration(mStage2TransitionSpeed);
        mStep2ChangeBoundsFavourites.setDuration(mStage2TransitionSpeed);
        mStep1OpenChangeBounds.setDuration(mStage1OpenTransitionSpeed);
        mStep1CloseChangeBounds.setDuration(mStage1CloseTransitionSpeed);


        mStep1CloseChangeBounds.setInterpolator(mAccelerateInterpolator);
        mStep2ChangeBoundsRated.setInterpolator(mOverShoot);
        mStep2ChangeBoundsPopular.setInterpolator(mOverShoot);
        mStep2ChangeBoundsFavourites.setInterpolator(mOverShoot);
        // setting up Transition listeners
        mStep1OpenChangeBounds.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
                mBindingMain.topRatedButton.setVisibility(View.VISIBLE);
                mBindingMain.popularButton.setVisibility(View.VISIBLE);
                mBindingMain.scrimScreen.setVisibility(View.VISIBLE);
                mAnimationIsRunning = true;
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                String filmsDisplaying = mSharedPrefs.getString(Constants.FILM_LIST_TO_DISPLAY, Constants.MOST_POPULAR);
                switch (filmsDisplaying) {
                    case Constants.MOST_POPULAR:
                        mElegantButtonCloseId = R.id.popular_button;
                        startStep2OpenPopular();
                        break;
                    case Constants.TOP_RATED:
                        mElegantButtonCloseId = R.id.top_rated_button;
                        startStep2OpenRated();
                        break;
                    default:
                        mElegantButtonCloseId = R.id.favourites_button;
                        startStep2OpenFavourites();
                }
            }

            @Override
            public void onTransitionCancel(Transition transition) {
            }

            @Override
            public void onTransitionPause(Transition transition) {
            }

            @Override
            public void onTransitionResume(Transition transition) {
            }
        });





        mStep1CloseChangeBounds.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
               mAnimationIsRunning = true;
                mBindingMain.progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                mClosing = false;
                setFabAppearance(mSharedPrefs.getString(Constants.FILM_LIST_TO_DISPLAY, Constants.MOST_POPULAR));
                mBindingMain.scrimScreen.setVisibility(View.GONE);
                mOptionsAreOpen = false;
                mAnimationIsRunning = false;
                mBindingMain.topRatedButton.setVisibility(View.GONE);
                mBindingMain.popularButton.setVisibility(View.GONE);
                mBindingMain.favouritesButton.setVisibility(View.GONE);
                String displayList = mSharedPrefs.getString(Constants.FILM_LIST_TO_DISPLAY, Constants.MOST_POPULAR);
                mBindingMain.progressBar.setVisibility(View.GONE);
                switch (displayList) {
                    case Constants.MOST_POPULAR:
                        userWarnings(viewModel.getPopularList(), Constants.WARNING_TYPE_INTERNET);
                        break;
                    case Constants.TOP_RATED:
                        userWarnings(viewModel.getRatedList(), Constants.WARNING_TYPE_INTERNET);
                        break;
                    default:
                        userWarnings(viewModel.getFavouritesList(), Constants.WARNING_TYPE_FAVOURITES);


                }
            }

            @Override
            public void onTransitionCancel(Transition transition) {
            }

            @Override
            public void onTransitionPause(Transition transition) {
            }

            @Override
            public void onTransitionResume(Transition transition) {
            }
        });
        mStep2ChangeBoundsPopular.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
                mAnimationIsRunning = true;
                if (mClosing && mElegantButtonCloseId != R.id.popular_button) {
                    mElegantCloseButton = findViewById(mElegantButtonCloseId);
                    mElegantCloseButton.setImageResource(R.color.transparent);

                }
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                mOptionsAreOpen = true;
                if (mClosing) {
                    mBindingMain.topRatedButton.setImageResource(R.drawable.svg_rated_grey_circle_icon);
                    mBindingMain.favouritesButton.setImageResource(R.drawable.svg_favs_grey_circle_icon);
                }
                mAnimationIsRunning = false;
                if (mClosing) {
                    startStep1CloseAnimation();
                }
            }

            @Override
            public void onTransitionCancel(Transition transition) {
            }

            @Override
            public void onTransitionPause(Transition transition) {
            }

            @Override
            public void onTransitionResume(Transition transition) {
            }
        });
        mStep2ChangeBoundsRated.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
                mAnimationIsRunning = true;
                if (mClosing && mElegantButtonCloseId != R.id.top_rated_button) {
                    mElegantCloseButton = findViewById(mElegantButtonCloseId);
                    mElegantCloseButton.setImageResource(R.color.transparent);

                }
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                mOptionsAreOpen = true;
                mBindingMain.popularButton.setImageResource(R.drawable.svg_popular_grey_circle_icon);
                mBindingMain.favouritesButton.setImageResource(R.drawable.svg_favs_grey_circle_icon);
                mAnimationIsRunning = false;
                if (mClosing) {
                    startStep1CloseAnimation();
                }
            }

            @Override
            public void onTransitionCancel(Transition transition) {
            }

            @Override
            public void onTransitionPause(Transition transition) {
            }

            @Override
            public void onTransitionResume(Transition transition) {
            }
        });

        mStep2ChangeBoundsFavourites.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
                mAnimationIsRunning = true;
                if (mClosing && mElegantButtonCloseId != R.id.favourites_button) {
                    mElegantCloseButton = findViewById(mElegantButtonCloseId);
                    mElegantCloseButton.setImageResource(R.color.transparent);

                }
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                mOptionsAreOpen = true;
                mBindingMain.popularButton.setImageResource(R.drawable.svg_popular_grey_circle_icon);
                mBindingMain.topRatedButton.setImageResource(R.drawable.svg_rated_grey_circle_icon);
                mAnimationIsRunning = false;
                if (mClosing) {
                    startStep1CloseAnimation();
                }
            }

            @Override
            public void onTransitionCancel(Transition transition) {
            }

            @Override
            public void onTransitionPause(Transition transition) {
            }

            @Override
            public void onTransitionResume(Transition transition) {
            }
        });
    }

    // constraint animation methods
    private void startStep1OpenAnimation() {
        TransitionManager.beginDelayedTransition(mConstraintLayout, mStep1OpenChangeBounds);
        mConstraintSetStep1.applyTo(mConstraintLayout);
        mBindingMain.topRatedButton.setImageResource(R.drawable.svg_rated_grey_circle_icon);
        mBindingMain.popularButton.setImageResource(R.drawable.svg_popular_grey_circle_icon);
        mBindingMain.favouritesButton.setImageResource(R.drawable.svg_favs_grey_circle_icon);
    }



    private void startStep1CloseAnimation() {
        TransitionManager.beginDelayedTransition(mConstraintLayout, mStep1CloseChangeBounds);
        mConstraintSetStart.applyTo(mConstraintLayout);
    }

    private void startStep2OpenFavourites() {
        mBindingMain.favouritesButton.setImageResource(R.drawable.svg_favs_red_big_chunk);
        TransitionManager.beginDelayedTransition(mConstraintLayout, mStep2ChangeBoundsFavourites);
        mConstraintSetStep2OpenFavourites.applyTo(mConstraintLayout);
    }


    private void startStep2OpenRated() {
        mBindingMain.topRatedButton.setImageResource(R.drawable.svg_rated_orange_big_chunk);
        TransitionManager.beginDelayedTransition(mConstraintLayout, mStep2ChangeBoundsRated);
        mConstraintSetStep2OpenRated.applyTo(mConstraintLayout);
    }

    private void startStep2OpenPopular() {
        mBindingMain.popularButton.setImageResource(R.drawable.svg_popular_pink_big_chunk);
        TransitionManager.beginDelayedTransition(mConstraintLayout, mStep2ChangeBoundsPopular);
        mConstraintSetStep2OpenPopular.applyTo(mConstraintLayout);
    }

    private void setUpViewModel() {

        viewModel = ViewModelProviders.of(this).get(MainActivityFilmViewModel.class);

        viewModel.getTopRated().observe(this, new Observer<List<Film>>() {
            @Override
            public void onChanged(@Nullable List<Film> films) {
                if (mSharedPrefs.getString(Constants.FILM_LIST_TO_DISPLAY, Constants.MOST_POPULAR).equals(Constants.TOP_RATED)) {
                    userWarnings(films, Constants.WARNING_TYPE_INTERNET);
                    mBindingMain.progressBar.setVisibility(View.GONE);
                }
            }

        });

        viewModel.getMostPopular().observe(this, new Observer<List<Film>>() {
            @Override
            public void onChanged(@Nullable List<Film> films) {
                if (mSharedPrefs.getString(Constants.FILM_LIST_TO_DISPLAY, Constants.MOST_POPULAR).equals(Constants.MOST_POPULAR)) {
                    userWarnings(films, Constants.WARNING_TYPE_INTERNET);
                    mBindingMain.progressBar.setVisibility(View.GONE);
                }
            }
        });

        viewModel.getFavourites().observe(this, new Observer<List<Film>>() {
            @Override
            public void onChanged(@Nullable List<Film> films) {
                if (mSharedPrefs.getString(Constants.FILM_LIST_TO_DISPLAY, Constants.MOST_POPULAR).equals(Constants.FAVOURITES)) {
                    userWarnings(films, Constants.WARNING_TYPE_FAVOURITES);
                    mBindingMain.progressBar.setVisibility(View.GONE);
                }
            }
        });

    }


    // setting up adapter method
    private void setUpAdapter() {
        View.OnClickListener itemListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Film film = (Film) v.getTag();
                mIntent = new Intent(MainActivity.this, DetailsActivity.class);
                mIntent.putExtra(Constants.FILM_OBJECT, film);
                startActivity(mIntent);
            }
        };
        mBindingMain.recyclerview.setLayoutManager(mLayoutManager);
        mAdapter = new FilmRecyclerAdapter(itemListener);
        //   mAdapter = new FilmRecyclerAdapter(itemListener);
        mBindingMain.recyclerview.setAdapter(mAdapter);
    }

    // method to set fab appearance according to shared preferences
    @SuppressLint("ApplySharedPref")
    private void setFabAppearance(String movieTypes) {
        switch (movieTypes) {
            case Constants.MOST_POPULAR:
                mBindingMain.floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#b22249")));
                mBindingMain.floatingActionButton.setImageResource(R.drawable.white_thumbs_up);
                break;
            case Constants.TOP_RATED:
                mBindingMain.floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ff8e0a")));
                mBindingMain.floatingActionButton.setImageResource(R.drawable.white_star);
                break;
            case Constants.FAVOURITES:
                mBindingMain.floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#e95974")));
                mBindingMain.floatingActionButton.setImageResource(R.drawable.white_heart);
                break;
        }
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


    @SuppressLint("ClickableViewAccessibility")
    private void setUpListeners() {
        View.OnTouchListener scrimListener = new View.OnTouchListener() {


            @Override
            public boolean onTouch(View v, MotionEvent event) {

                int id = v.getId();
                if (!mAnimationIsRunning && id == R.id.scrim_screen) {
                    startStep1CloseAnimation();
                }
                return true;
            }

        };
        mBindingMain.scrimScreen.setOnTouchListener(scrimListener);
        mBindingMain.noInternetScrim.setOnTouchListener(scrimListener);
        // setting up main UI listener
        View.OnClickListener menuListener = new View.OnClickListener() {
            @SuppressLint("ApplySharedPref")
            @Override
            public void onClick(View v) {
                if (mAnimationIsRunning) {
                    return;
                }
                int id = v.getId();
                switch (id) {
                    case R.id.floatingActionButton:
                        if (!mOptionsAreOpen) {
                            startStep1OpenAnimation();
                        } else {
                            startStep1CloseAnimation();
                        }
                        break;
                    case R.id.top_rated_button:
                        if (!mAnimationIsRunning) {
                            mClosing = true;
                            mSharedPrefs.edit().putString(Constants.FILM_LIST_TO_DISPLAY, Constants.TOP_RATED).commit();
                            startStep2OpenRated();
                        }
                        break;
                    case R.id.popular_button:
                        if (!mAnimationIsRunning) {
                            mClosing = true;
                            mSharedPrefs.edit().putString(Constants.FILM_LIST_TO_DISPLAY, Constants.MOST_POPULAR).commit();
                            startStep2OpenPopular();
                        }
                        break;
                    case R.id.refresh_button:
                        if (!checkInternet()) {
                            Toast.makeText(MainActivity.this, R.string.refresh_warning, Toast.LENGTH_LONG).show();

                        } else {
                            viewModel.refreshOnlineData();
                        }
                        break;
                    case R.id.ok_button:
                        mBindingMain.noInternetScrim.setVisibility(View.GONE);
                        mBindingMain.noInternetWarningCardview.setVisibility(View.GONE);
                        break;
                    case R.id.favourites_button:
                        if (!mAnimationIsRunning) {
                            mClosing = true;
                            mSharedPrefs.edit().putString(Constants.FILM_LIST_TO_DISPLAY, Constants.FAVOURITES).commit();
                            startStep2OpenFavourites();
                        }
                        break;
                }
            }
        };
        // setting listener on UI elements
        mBindingMain.okButton.setOnClickListener(menuListener);
        mBindingMain.floatingActionButton.setOnClickListener(menuListener);
        mBindingMain.refreshButton.setOnClickListener(menuListener);
        mBindingMain.popularButton.setOnClickListener(menuListener);
        mBindingMain.topRatedButton.setOnClickListener(menuListener);
        mBindingMain.favouritesButton.setOnClickListener(menuListener);

    }


}


