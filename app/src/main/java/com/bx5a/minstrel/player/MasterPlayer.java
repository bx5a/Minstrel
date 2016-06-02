/*
 * Copyright Guillaume VINCKE 2016
 *
 * This file is part of Minstrel
 *
 * Minstrel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Minstrel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Minstrel.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.bx5a.minstrel.player;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton representing the main audio player
 * That element lets you enqueue a song, play, pause ...
 */
public class MasterPlayer {
    private static MasterPlayer instance;
    private PlaylistManager playlistManager;
    private ArrayList<Player> players;
    private ArrayList<MasterPlayerEventListener> listeners;
    private boolean autoPlayNext;
    private boolean playlistFinished;

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

        playlistManager.getPlaylist().setEventListener(new Playlist.EventListener() {
            @Override
            public void onChanged() {
                notifyPlaylistChanged();
            }
        });

        playlistManager.setSelectedIndexEventListener(new PlaylistManager.SelectedIndexEventListener() {
            @Override
            public void onChanged() {
                notifyPlaylistChanged();
            }
        });

        autoPlayNext = true;
        playlistFinished = false;
    }

    public Playlist getPlaylist() {
        return playlistManager.getPlaylist();
    }

    public void setPlaylistManagerEventListener(PlaylistManager.EventListener eventListener) {
        playlistManager.setEventListener(eventListener);
    }

    public void setCurrentPlayableIndex(int currentPlayableIndex)
            throws IndexOutOfBoundsException, IllegalStateException {
        pause();
        playlistManager.move(currentPlayableIndex);
        play();
    }

    public int getCurrentPlayableIndex() {
        return playlistManager.getSelectedIndex();
    }

    public void enqueue(Playable playable, Position position)
            throws IndexOutOfBoundsException, IllegalStateException {
        playlistManager.enqueue(playable, position);

        if (!autoPlayNext) {
            return;
        }
        autoPlayNext = false;

        // if playlist finished, we still have the last one selected. We need to move to next first
        if (playlistFinished) {
            playlistFinished = false;
            next();
            return;
        }
        play();
    }

    /**
     * Enqueue playable's most related song
     * @note That function is asynchronous
     * @param playable
     * @param position
     */
    public void enqueueRelated(Playable playable, final Position position) {
        playable.asyncGetRelated(new Playable.RelatedAvailableListener() {
            @Override
            public void onRelatedAvailable(List<Playable> related) {
                if (related.size() == 0) {
                    Log.w("PlaylistManager", "No related found can't auto enqueue");
                    return;
                }
                enqueue(related.get(0), position);
            }
        });
    }

    public void playAt(final float seekValue) throws IndexOutOfBoundsException, IllegalStateException {
        autoPlayNext = false;
        playlistFinished = false;

        final Playable playable = playlistManager.getSelected();
        if (playable == null) {
            throw new IllegalStateException("Can't access selected playable");
        }

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
                        notifyPlaylistFinished();
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

    public void next() throws IndexOutOfBoundsException {
        pause();
        playlistManager.next();
        play();
    }

    public void previous() throws IndexOutOfBoundsException {
        pause();
        playlistManager.previous();
        play();
    }

    // position is a [0, 1] value
    public void seekTo(float position) throws IndexOutOfBoundsException, IllegalStateException {
        Playable playable = playlistManager.getSelected();
        if (playable == null) {
            throw new IllegalStateException("Can't access selected playable");
        }
        playable.seekTo(position);
    }

    public float getCurrentPosition() {
        for (Player player: players) {
            if (player.isPlaying()) {
                return player.getCurrentPosition();
            }
        }
        return 0;
    }

    public void reorder(int playableIndex, Position position) throws IndexOutOfBoundsException {
        playlistManager.reorder(playableIndex, position);
    }

    public void remove(int playableIndex) throws IndexOutOfBoundsException {
        playlistManager.remove(playableIndex);
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

    public void notifyPlaylistFinished() {
        playlistFinished = true;
        for (MasterPlayerEventListener listener : listeners) {
            listener.onPlaylistFinish();
        }
    }
}
