package com.bx5a.minstrel.player;

/**
 * Created by guillaume on 25/03/2016.
 */
public interface Playable {
    void play();

    void pause();

    void resume();

    String title();

    // position is a value [0, 1]. 0 being the beginning of the song and 1 the end
    void seekTo(float position);
}
