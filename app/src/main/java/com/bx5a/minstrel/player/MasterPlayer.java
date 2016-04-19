package com.bx5a.minstrel.player;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;

// Singleton
public class MasterPlayer {
    private static MasterPlayer instance;
    private Playlist playlist;
    private int currentPlayableIndex;
    private Context context;
    private ArrayList<Player> players;

    public Playlist getPlaylist() {
        return playlist;
    }

    public void setCurrentPlayableIndex(int currentPlayableIndex) throws IndexOutOfBoundsException {
        if(playlist.size() <= currentPlayableIndex || currentPlayableIndex < 0) {
            throw new IndexOutOfBoundsException("No playable at index " + currentPlayableIndex);
        }
        pause();
        this.currentPlayableIndex = currentPlayableIndex;
        notifyPlaylistChanged();
        play();
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
        context = null;
        players = new ArrayList<>();
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void enqueue(Playable playable, Position position) {
        if (position == Position.Next && playlist.size() != 0) {
            playlist.add(playable, currentPlayableIndex + 1);
            notifyPlaylistChanged();
            return;
        }
        playlist.add(playable);
        notifyPlaylistChanged();
    }

    public void play() throws IndexOutOfBoundsException {
        playlist.at(currentPlayableIndex).play();
    }

    public void pause() throws IndexOutOfBoundsException {
        playlist.at(currentPlayableIndex).pause();
    }

    public boolean isPlaying() {
        for (Player player: players) {
            if (player.isPlaying()) {
                return true;
            }
        }
        return false;
    }

    public void next() throws IndexOutOfBoundsException {
        setCurrentPlayableIndex(currentPlayableIndex + 1);
    }

    public void previous() throws IndexOutOfBoundsException {
        setCurrentPlayableIndex(currentPlayableIndex - 1);
    }

    // position is a [0, 1] value
    public void seekTo(float position) throws IndexOutOfBoundsException {
        playlist.at(currentPlayableIndex).seekTo(position);
    }

    private void notifyPlaylistChanged() {
        if (context == null) {
            // TODO: assert ?
            Log.e("MasterPlayer", "Couldn't notify because context isn't set");
            return;
        }
        Intent notificationIntent = new Intent("Minstrel.playlistChanged");
        context.sendBroadcast(notificationIntent);
    }

    public void registerPlayer(Player player) {
        players.add(player);
    }
}
