package com.bx5a.minstrel.player;

import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;

// Singleton
public class Player {

    private static Player instance;
    private ArrayList<Playable> playlist;
    private int currentPlayableIndex;

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
            notifyNextSongChanged(context);
            return;
        }
        playlist.add(playable);
    }

    public void play(Context context) {
        if (playlist.size() <= currentPlayableIndex) {
            // TODO: log error
            return;
        }
        playlist.get(currentPlayableIndex).play();
        notifyCurrentSongChanged(context);
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
        play(context);
    }

    public void previous(Context context) {
        if (currentPlayableIndex == 0) {
            return;
        }
        pause(context);
        currentPlayableIndex--;
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

    private void notifyCurrentSongChanged(Context context) {
        if (playlist.size() <= currentPlayableIndex) {
            // TODO: log error
            return;
        }
        Intent notificationIntent = new Intent("Minstrel.currentSongChanged");
        notificationIntent.putExtra("Title", playlist.get(currentPlayableIndex).title());
        context.sendBroadcast(notificationIntent);
    }
    private void notifyNextSongChanged(Context context) {
        if (playlist.size() <= currentPlayableIndex + 1) {
            // TODO: log error
            return;
        }
        Intent notificationIntent = new Intent("Minstrel.nextSongChanged");
        notificationIntent.putExtra("Title", playlist.get(currentPlayableIndex + 1).title());
        context.sendBroadcast(notificationIntent);
    }
}
