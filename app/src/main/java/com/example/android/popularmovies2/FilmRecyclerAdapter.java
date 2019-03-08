package com.example.android.popularmovies2;


import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.example.android.popularmovies2.RepositoryUtilities.FilmType;
import com.example.android.popularmovies2.database.Film;
import com.squareup.picasso.Picasso;

import java.util.List;

public class FilmRecyclerAdapter extends RecyclerView.Adapter<FilmRecyclerAdapter.FilmViewHolder> {


    private List<Film> items;
    private View.OnClickListener itemListener;
    private FilmType mFilmType;
    private int viewType;
    private int mLayout;

    FilmRecyclerAdapter(View.OnClickListener itemListener) {

        this.itemListener = itemListener;
    }

    @Override
    public int getItemCount() {
        if(items == null){
            return 0;
        }else{
            return items.size();
        }

    }

    @NonNull
    @Override
    public FilmViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {


        View view = LayoutInflater.from(viewGroup.getContext()).inflate(mLayout, viewGroup, false);
        return new FilmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FilmViewHolder viewHolder, int i) {
        Picasso.with(viewHolder.itemView.getContext())
                .load(items.get(i).getThumbnailUrl())
                .error(R.drawable.movieapp_image_placeholder_200)
                .into(viewHolder.posterImageView);
        viewHolder.layout.setTag(items.get(i));
        viewHolder.movieTitleTextView.setText(items.get(i).getTitle());
        viewHolder.filmTypeIcon.setImageResource(items.get(i).getIconGraphicId());
        viewHolder.ratingTextView.setText(items.get(i).getVoteAverage());
    }
    public void setDataList(List<Film> films) {
        items = films;
        if(items != null && items.size() > 0) {
            FilmType filmType = items.get(0).getFilmType();

            switch(filmType){
                case POPULAR:
                    mLayout = R.layout.popular_list_item_layout;
                    viewType = 1;
                    break;
                case FAVOURITE:
                    mLayout = R.layout.favourite_list_item_layout;
                    viewType = 2;
                    break;
                default:
                    mLayout = R.layout.rated_list_item_layout;
                    viewType = 3;
            }}
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return viewType;
    }

    class FilmViewHolder extends RecyclerView.ViewHolder {
        ImageView posterImageView;
        TextView movieTitleTextView;
        View layout;
        ImageView filmTypeIcon;
        TextView ratingTextView;

        FilmViewHolder(View view) {
            super(view);
            this.posterImageView = view.findViewById(R.id.poster_image_view);
            this.movieTitleTextView = view.findViewById(R.id.movie_title_text_view);
            this.layout = view;
            this.filmTypeIcon = view.findViewById(R.id.flim_type_icon);
            this.ratingTextView = view.findViewById(R.id.rating_text_view);
            view.setOnClickListener(itemListener);
        }
    }
}







