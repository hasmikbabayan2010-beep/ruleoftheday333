package com.example.ruleoftheday333.adapters;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ruleoftheday333.R;
import com.example.ruleoftheday333.models.Song;

import java.io.IOException;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private Context context;
    private List<Song> songs;
    private MediaPlayer mediaPlayer;
    private int currentlyPlayingPosition = -1;

    public SongAdapter(Context context, List<Song> songs) {
        this.context = context;
        this.songs = songs;
        this.mediaPlayer = new MediaPlayer();
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_song, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songs.get(position);

        holder.txtSong.setText(song.getName() != null ? song.getName() : "Unknown");
        holder.txtArtist.setText(song.getArtist() != null ? song.getArtist() : "Unknown");

        // Load album cover safely
        Glide.with(holder.itemView.getContext())
                .load(song.getAlbumCoverUrl() != null ? song.getAlbumCoverUrl() : R.drawable.placeholder_album_foreground)
                .placeholder(R.drawable.placeholder_album_foreground)
                .into(holder.imgAlbum);

        holder.itemView.setOnClickListener(v -> {
            try {
                // If same item clicked → toggle pause/play
                if (currentlyPlayingPosition == position && mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    return;
                }

                // Stop current playback safely
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }

                mediaPlayer.reset();

                if (song.getPreviewUrl() == null || song.getPreviewUrl().isEmpty()) {
                    return;
                }

                mediaPlayer.setDataSource(song.getPreviewUrl());

                mediaPlayer.setOnPreparedListener(mp -> {
                    mp.start();
                    currentlyPlayingPosition = position;
                });

                mediaPlayer.setOnCompletionListener(mp -> {
                    currentlyPlayingPosition = -1;
                });

                mediaPlayer.prepareAsync();

            } catch (IOException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public int getItemCount() {
        return songs != null ? songs.size() : 0;
    }

    public void releasePlayer() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAlbum;
        TextView txtSong, txtArtist;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAlbum = itemView.findViewById(R.id.imgAlbum);
            txtSong = itemView.findViewById(R.id.txtSong);
            txtArtist = itemView.findViewById(R.id.txtArtist);
        }
    }
}