package com.bx5a.minstrel.youtube;

import android.util.Log;

import com.google.android.youtube.player.YouTubePlayer;

/**
 * Created by guillaume on 11/04/2016.
 */
public class Player {
    private static Player ourInstance = new Player();

    private YouTubePlayer youtubePlayer;

    public static Player getInstance() {
        return ourInstance;
    }

    private Player() {
        youtubePlayer = null;
    }

    public YouTubePlayer getYoutubePlayer() {
        return youtubePlayer;
    }

    public void setYoutubePlayer(YouTubePlayer youtubePlayer) {
        this.youtubePlayer = youtubePlayer;
    }

    public boolean play(YoutubeVideo video) {
        if (youtubePlayer == null) {
            return false;
        }
        Log.i("Youtube.player", "Playing " + video.title() + " " + video.getId());
        youtubePlayer.loadVideo(video.getId());
        youtubePlayer.play();
        return true;
    }
}
