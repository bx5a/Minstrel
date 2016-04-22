package com.bx5a.minstrel.player;

import android.util.Log;

import java.util.ArrayList;

// Singleton
public class MasterPlayer {
    // event listener for playable enqueued
    public interface OnPlayableEnqueuedListener {
        void onEnqueued(Playable playable, Position position);
    }

    private static MasterPlayer instance;
    private Playlist playlist;
    private int currentPlayableIndex;
    private ArrayList<Player> players;
    private ArrayList<MasterPlayerEventListener> listeners;
    private OnPlayableEnqueuedListener onEnqueuedListener;

    public static MasterPlayer getInstance() {
        if (instance == null) {
            instance = new MasterPlayer();
        }
        return instance;
    }

    private MasterPlayer() {
        playlist = new Playlist();
        currentPlayableIndex = 0;
        players = new ArrayList<>();
        listeners = new ArrayList<>();
        onEnqueuedListener = new OnPlayableEnqueuedListener() {
            @Override
            public void onEnqueued(Playable playable, Position position) {

            }
        };
    }

    public Playlist getPlaylist() {
        return playlist;
    }

    public void setCurrentPlayableIndex(int currentPlayableIndex) throws IndexOutOfBoundsException {
        pause();
        if(playlist.size() <= currentPlayableIndex || currentPlayableIndex < 0) {
            throw new IndexOutOfBoundsException("No playable at index " + currentPlayableIndex);
        }
        this.currentPlayableIndex = currentPlayableIndex;
        notifyPlaylistChanged();
        play();
    }

    public void setOnEnqueuedListener(OnPlayableEnqueuedListener onEnqueuedListener) {
        this.onEnqueuedListener = onEnqueuedListener;
    }

    public int getCurrentPlayableIndex() {
        return currentPlayableIndex;
    }

    public void enqueue(Playable playable, Position position) {
        if (position == Position.Next && playlist.size() != 0) {
            playlist.add(playable, currentPlayableIndex + 1);
            notifyPlaylistChanged();
            onEnqueuedListener.onEnqueued(playable, position);
            return;
        }
        playlist.add(playable);
        notifyPlaylistChanged();
        onEnqueuedListener.onEnqueued(playable, position);

        // TODO: find a way to generalize that very unusual behavior
        // if nothing was playing, start playback (also if we want to play now)
        if (playlist.size() == 1) {
            play();
        }
    }

    public void dequeue(Playable playable, Position position) throws IndexOutOfBoundsException {
        int indexToRemove = currentPlayableIndex;
        if (position == Position.Next && playlist.size() != 1) {
            indexToRemove = currentPlayableIndex + 1;
        }

        // only remove if we have the right playable at that index
        if (playlist.at(indexToRemove).getId() == playable.getId()) {
            playlist.remove(indexToRemove);
            notifyPlaylistChanged();
        }
    }

    public void play() throws IndexOutOfBoundsException {
        Playable playable = playlist.at(currentPlayableIndex);
        if (!playable.isLoaded()) {
            playable.load();
            try {
                History.getInstance().store(playable);
            } catch (NullPointerException exception) {
                Log.i("MasterPlayer", "Couldn't store playable: " + exception.getMessage());
            }
        }
        playable.play();
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

    public void next() {
        setCurrentPlayableIndex(currentPlayableIndex + 1);
    }

    public void previous() throws IndexOutOfBoundsException {
        setCurrentPlayableIndex(currentPlayableIndex - 1);
    }

    // position is a [0, 1] value
    public void seekTo(float position) throws IndexOutOfBoundsException {
        playlist.at(currentPlayableIndex).seekTo(position);
    }

    public float getCurrentPosition() {
        for (Player player: players) {
            if (player.isPlaying()) {
                return player.getCurrentPosition();
            }
        }
        return 0;
    }

    public void registerPlayer(Player player) {
        players.add(player);
    }

    public void unregisterPlayer(Player player) {
        players.remove(player);
    }

    public void addMasterPlayerEventListener(MasterPlayerEventListener listener) {
        listeners.add(listener);
    }

    public void removeMasterPlayerEventListener(MasterPlayerEventListener listener) {
        listeners.remove(listener);
    }

    private void notifyPlaylistChanged() {
        for (MasterPlayerEventListener listener : listeners) {
            listener.onPlaylistChange();
        }
    }

    public void notifyPlayStateChanged() {
        for (MasterPlayerEventListener listener : listeners) {
            listener.onPlayStateChange();
        }
    }

    public void notifyCurrentTimeChanged() {
        for (MasterPlayerEventListener listener : listeners) {
            listener.onCurrentTimeChange();
        }
    }
}
