package com.bx5a.minstrel.player;

import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;

// Singleton
public class Player {
    private static Player instance;
    private ArrayList<Playable> playlist;
    private int currentPlayableIndex;

    public ArrayList<Playable> getPlaylist() {
        return playlist;
    }

    public int getCurrentPlayableIndex() {
        return currentPlayableIndex;
    }

    public static Player getInstance() {
        if (instance == null) {
            instance = new Player();
        }
        return instance;
    }

    private Player() {
        playlist = new ArrayList<>();
        currentPlayableIndex = 0;
    }

    public void enqueue(Context context, Playable playable, Position position) {
        if (position == Position.Next && playlist.size() != 0) {
            playlist.add(currentPlayableIndex + 1, playable);
            notifyPlaylistChanged(context);
            return;
        }
        playlist.add(playable);
        notifyPlaylistChanged(context);
    }

    public void play(Context context) {
        if (playlist.size() <= currentPlayableIndex) {
            // TODO: log error
            return;
        }
        playlist.get(currentPlayableIndex).play();
    }

    public void pause(Context context) {
        if (playlist.size() <= currentPlayableIndex) {
            // TODO: log error
            return;
        }
        playlist.get(currentPlayableIndex).pause();
    }

    public void next(Context context) {
        if (playlist.size() <= currentPlayableIndex + 1) {
            return;
        }
        pause(context);
        currentPlayableIndex++;
        notifyPlaylistChanged(context);
        play(context);
    }

    public void previous(Context context) {
        if (currentPlayableIndex == 0) {
            return;
        }
        pause(context);
        currentPlayableIndex--;
        notifyPlaylistChanged(context);
        play(context);
    }

    // position is a [0, 1] value
    public void seek(float position) {
        if (playlist.size() <= currentPlayableIndex) {
            // TODO: log error
            return;
        }
        playlist.get(currentPlayableIndex).seek(position);
    }

    private void notifyPlaylistChanged(Context context) {
        if (playlist.size() <= currentPlayableIndex) {
            // TODO: log error
            return;
        }
        Intent notificationIntent = new Intent("Minstrel.playlistChanged");
        context.sendBroadcast(notificationIntent);
    }
}
