package com.bx5a.minstrel.player;

import android.content.Context;

/**
 * Created by guillaume on 25/03/2016.
 */
public interface Playable {
    // Let you initialize only for its id
    void initFromId(String id, Context context);

    void load();

    boolean isLoaded();

    void play();

    void pause();

    String title();

    // position is a value [0, 1]. 0 being the beginning of the song and 1 the end
    void seekTo(float position);

    String getId();

    // can return an empty string if no thumbnail available
    String getThumbnailURL();
}
