package com.bx5a.minstrel.youtube;

import com.bx5a.minstrel.Video;

/**
 * Created by guillaume on 24/03/2016.
 */
public class YoutubeVideo extends Video {
    @Override
    public void load() {
        YoutubePlayer.getInstance().load(this);
    }

    @Override
    public boolean isLoaded() {
        String playingId = YoutubePlayer.getInstance().getLoadedId();
        if (playingId.isEmpty()) {
            return false;
        }
        return getId().equals(playingId);
    }

    @Override
    public void play() {
        YoutubePlayer.getInstance().play();
    }

    @Override
    public void pause() {
        YoutubePlayer.getInstance().pause();
    }

    @Override
    public String title() {
        return this.title;
    }

    @Override
    public void seekTo(float position) {
        YoutubePlayer.getInstance().seekTo(position);
    }
}
