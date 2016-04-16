package com.bx5a.minstrel.player;

/**
 * Created by guillaume on 16/04/2016.
 */
public interface Player {

    void play();
    void pause();
    void resume();
    // position is a [0, 1] value
    void seekTo(float position);
    boolean isPlaying();

}
