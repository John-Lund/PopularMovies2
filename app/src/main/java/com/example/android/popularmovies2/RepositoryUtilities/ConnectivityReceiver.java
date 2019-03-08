package com.example.android.popularmovies2.RepositoryUtilities;


import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.android.popularmovies2.FilmRepository;

public class ConnectivityReceiver extends BroadcastReceiver{
    private Application application;
    private Context mContext;

    public ConnectivityReceiver(Application application, Context context) {
        this.application = application;
        this.mContext = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if(checkInternet()){
            FilmRepository.getFilmRepository(application).refreshOnlineData();

        }





    }

    private boolean checkInternet() {
        ConnectivityManager manager =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork;
        activeNetwork = (manager != null) ? manager.getActiveNetworkInfo() : null;
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }
}
