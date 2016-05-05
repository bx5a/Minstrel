package com.bx5a.minstrel.player;

/**
 * Created by guillaume on 16/04/2016.
 */
public interface Player {
    interface OnInitializedListener {
        void onInitializationSuccess();
        void onInitializationFailure(String reason);
    }

    boolean isInitialized();
    void initialize(OnInitializedListener listener);

    void play();
    void pause();
    // position is a [0, 1] value
    void seekTo(float position);
    float getCurrentPosition();
    boolean isPlaying();

}
