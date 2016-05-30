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
    private PlaylistManager playlistManager;
    private ArrayList<Player> players;
    private ArrayList<MasterPlayerEventListener> listeners;
    private OnPlayableEnqueuedListener onEnqueuedListener;
    private boolean autoPlayNext;

    public static MasterPlayer getInstance() {
        if (instance == null) {
            instance = new MasterPlayer();
        }
        return instance;
    }

    private MasterPlayer() {
        playlistManager = new PlaylistManager(new Playlist());
        players = new ArrayList<>();
        listeners = new ArrayList<>();
        onEnqueuedListener = new OnPlayableEnqueuedListener() {
            @Override
            public void onEnqueued(Playable playable, Position position) {

            }
        };
        autoPlayNext = true;
    }

    public Playlist getPlaylist() {
        return playlistManager.getPlaylist();
    }

    public void setCurrentPlayableIndex(int currentPlayableIndex) throws IndexOutOfBoundsException, IllegalStateException {
        pause();
        playlistManager.move(currentPlayableIndex);
        notifyPlaylistChanged();
        play();
    }

    public void setOnEnqueuedListener(OnPlayableEnqueuedListener onEnqueuedListener) {
        this.onEnqueuedListener = onEnqueuedListener;
    }

    public int getCurrentPlayableIndex() {
        return playlistManager.getSelectedIndex();
    }

    public void enqueue(Playable playable, Position position) throws IndexOutOfBoundsException, IllegalStateException {
        playlistManager.enqueue(playable, position);
        notifyPlaylistChanged();
        if (!autoPlayNext) {
            onEnqueuedListener.onEnqueued(playable, position);
            return;
        }
        autoPlayNext = false;
        play();
        notifyPlaylistChanged();
        onEnqueuedListener.onEnqueued(playable, Position.Current);
    }

    @Deprecated
    public void dequeue(Playable playable, Position position) throws IndexOutOfBoundsException, IllegalStateException {
        //int indexToRemove = getIndexFromPosition(position);
        // only remove if we have the right playable at that index
        //if (playlist.at(indexToRemove).getId() == playable.getId()) {
        //    playlist.remove(indexToRemove);
        //    notifyPlaylistChanged();
        //}
    }

    public void playAt(final float seekValue) throws IndexOutOfBoundsException, IllegalStateException {
        final Playable playable = playlistManager.getSelected();

        // initialize player if required
        if (!playable.getPlayer().isInitialized()) {
            playable.getPlayer().initialize(new Player.OnInitializedListener() {
                @Override
                public void onInitializationSuccess() {
                    try {
                        playAt(seekValue);
                    } catch (IndexOutOfBoundsException e) {
                        Log.w("MasterPlayer", "Cant start playing: " + e.getMessage());
                    }
                }

                @Override
                public void onInitializationFailure(String reason) {
                    Log.e("MasterPlayer", "Can't play current index: " + reason + ". Moving to next");
                    next();
                }
            }, new Player.OnPlayerStoppedListener() {
                @Override
                public void onPlayerStopped() {
                    // if playlist is empty now
                    if (playlistManager.getSelectedIndex() == playlistManager.size() - 1) {
                        autoPlayNext = true;
                        return;
                    }
                    try {
                        next();
                    } catch (IndexOutOfBoundsException exception) {
                        Log.i("MasterPlayer", "Couldn't move to next song: " + exception.getMessage());
                    }
                }
            });
            return;
        }

        if (!playable.isLoaded()) {
            playable.load();
            try {
                History.getInstance().store(playable);
            } catch (NullPointerException exception) {
                Log.i("MasterPlayer", "Couldn't store playable: " + exception.getMessage());
            }
        }

        playable.play();
        if (seekValue != 0) {
            seekTo(seekValue);
        }
    }

    public void play() throws IndexOutOfBoundsException, IllegalStateException {
        playAt(0);
    }

    public void pause() throws IndexOutOfBoundsException, IllegalStateException {
        playlistManager.getSelected().pause();
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
        pause();
        playlistManager.next();
        notifyPlaylistChanged();
        play();
    }

    public void previous() throws IndexOutOfBoundsException {
        pause();
        playlistManager.previous();
        notifyPlaylistChanged();
        play();
    }

    // position is a [0, 1] value
    public void seekTo(float position) throws IndexOutOfBoundsException, IllegalStateException {
        playlistManager.getSelected().seekTo(position);
    }

    public float getCurrentPosition() {
        for (Player player: players) {
            if (player.isPlaying()) {
                return player.getCurrentPosition();
            }
        }
        return 0;
    }

    public void reorder(int playableIndex, Position position) {
        playlistManager.reorder(playableIndex, position);
        notifyPlaylistChanged();
    }

    public void remove(int playableIndex) {
        //if (playableIndex == currentPlayableIndex) {
        //    if (currentPlayableIndex + 1 != playlist.size()) {
        //        // if that's not the last one
        //        next();
        //    } else if (currentPlayableIndex - 1 >= 0) {
        //        // if we have a previous one
        //        previous();
        //    } else {
        //        pause();
        //        autoPlayNext = true;
        //    }
        //}

        //playlist.remove(playableIndex);
        // update playable index if necessary
        //if (playableIndex < currentPlayableIndex) {
        //    currentPlayableIndex--;
        //}
        playlistManager.remove(playlistManager.getSelectedIndex());
        notifyPlaylistChanged();
    }

    public void registerPlayer(Player player) {
        players.add(player);
    }

    public void unregisterPlayer(Player player) {
        players.remove(player);
    }

    public void unload() {
        for (Player player : players) {
            player.unload();
        }
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
