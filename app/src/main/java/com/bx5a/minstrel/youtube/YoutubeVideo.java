package com.bx5a.minstrel.youtube;

import com.bx5a.minstrel.Video;

/**
 * Created by guillaume on 24/03/2016.
 */
public class YoutubeVideo extends Video {
    @Override
    public void play() {
        YoutubePlayer.getInstance().load(this);
        YoutubePlayer.getInstance().play();
    }

    @Override
    public void pause() {
        YoutubePlayer.getInstance().pause();
    }

    @Override
    public void resume() {
        YoutubePlayer.getInstance().play();
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
