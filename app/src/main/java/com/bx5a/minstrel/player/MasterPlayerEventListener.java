package com.bx5a.minstrel.player;

/**
 * Created by guillaume on 21/04/2016.
 */
public interface MasterPlayerEventListener {
    void onPlayStateChange();
    void onPlaylistChange();
    void onCurrentTimeChange();
    void onPlaylistFinish();
}
