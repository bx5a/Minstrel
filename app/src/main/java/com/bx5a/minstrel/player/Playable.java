package com.bx5a.minstrel.player;

import android.content.Context;

import java.io.IOException;

/**
 * Created by guillaume on 25/03/2016.
 */
public interface Playable {
    // Let you initialize only for its id
    void initFromId(String id) throws IOException;

    void load() throws IllegalStateException;

    boolean isLoaded();

    void play() throws IllegalStateException;

    void pause() throws IllegalStateException;

    String title();

    // position is a value [0, 1]. 0 being the beginning of the song and 1 the end
    void seekTo(float position) throws IllegalStateException;

    String getId();

    // can return an empty string if no thumbnail available
    String getThumbnailURL();

    String duration();

    Player getPlayer();
}
