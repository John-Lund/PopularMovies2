package com.example.android.popularmovies2;


import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.example.android.popularmovies2.RepositoryUtilities.VideoLink;
import com.squareup.picasso.Picasso;

import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {
    private List<VideoLink> videoLinks;
    private View.OnClickListener itemListener;

    public VideoAdapter(View.OnClickListener itemListener) {
        this.itemListener = itemListener;
    }

    public void setDataList(List<VideoLink> videoLink) {
        if (videoLink != null) {
            videoLinks = videoLink;
        }
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public VideoAdapter.VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_list_item, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoAdapter.VideoViewHolder holder, int position) {
        Picasso.with(holder.itemView.getContext())
                .load(videoLinks.get(position).getThumbnailUrlString())
                .error(R.drawable.movieapp_image_placeholder_200)
                .into(holder.thumbnail);
        holder.layout.setTag(videoLinks.get(position));
    }

    @Override
    public int getItemCount() {
        if (videoLinks == null) {
            return 0;
        } else {
            return videoLinks.size();
        }
    }

    class VideoViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        View layout;

        VideoViewHolder(View view) {
            super(view);
            this.thumbnail = view.findViewById(R.id.thumbnailImageView);
            this.layout = view;
            view.setOnClickListener(itemListener);
        }
    }
}
