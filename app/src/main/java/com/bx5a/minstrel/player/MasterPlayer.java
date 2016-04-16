package com.bx5a.minstrel.player;

import android.content.Context;
import android.content.Intent;

// Singleton
public class MasterPlayer {
    private static MasterPlayer instance;
    private Playlist playlist;
    private int currentPlayableIndex;

    public Playlist getPlaylist() {
        return playlist;
    }

    public int getCurrentPlayableIndex() {
        return currentPlayableIndex;
    }

    public static MasterPlayer getInstance() {
        if (instance == null) {
            instance = new MasterPlayer();
        }
        return instance;
    }

    private MasterPlayer() {
        playlist = new Playlist();
        currentPlayableIndex = 0;
    }

    public void enqueue(Context context, Playable playable, Position position) {
        if (position == Position.Next && playlist.size() != 0) {
            playlist.add(playable, currentPlayableIndex + 1);
            notifyPlaylistChanged(context);
            return;
        }
        playlist.add(playable);
        notifyPlaylistChanged(context);
    }

    public void play(Context context) throws IndexOutOfBoundsException {
        playlist.at(currentPlayableIndex).play();
    }

    public void pause(Context context) throws IndexOutOfBoundsException {
        playlist.at(currentPlayableIndex).pause();
    }

    public void next(Context context) throws IndexOutOfBoundsException {
        if (playlist.size() <= currentPlayableIndex + 1) {
            throw new IndexOutOfBoundsException("No next song available");
        }
        pause(context);
        currentPlayableIndex++;
        notifyPlaylistChanged(context);
        play(context);
    }

    public void previous(Context context) throws IndexOutOfBoundsException {
        if (currentPlayableIndex == 0) {
            throw new IndexOutOfBoundsException("No previous song available");
        }
        pause(context);
        currentPlayableIndex--;
        notifyPlaylistChanged(context);
        play(context);
    }

    // position is a [0, 1] value
    public void seekTo(float position) throws IndexOutOfBoundsException {
        playlist.at(currentPlayableIndex).seekTo(position);
    }

    private void notifyPlaylistChanged(Context context) {
        Intent notificationIntent = new Intent("Minstrel.playlistChanged");
        context.sendBroadcast(notificationIntent);
    }
}
