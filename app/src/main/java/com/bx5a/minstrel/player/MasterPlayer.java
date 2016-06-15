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

import com.bx5a.minstrel.exception.EmptyPlaylistException;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

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

    public void setCurrentPlayableIndex(int currentPlayableIndex) throws IndexOutOfBoundsException {
        if (!playlistManager.hasIndex(currentPlayableIndex)) {
            throw new IndexOutOfBoundsException("Playlist doesn't contain index " + String.valueOf(currentPlayableIndex));
        }
        pause();
        playlistManager.move(currentPlayableIndex);
        play();
    }

    public int getCurrentPlayableIndex() throws EmptyPlaylistException {
        if (playlistManager.isEmpty()) {
            throw new EmptyPlaylistException("Can't get selected index on an empty playlist");
        }
        return playlistManager.getSelectedIndex();
    }

    public void enqueue(Playable playable, Position position) {
        playlistManager.enqueue(playable, position);

        // if no auto play just return
        if (!autoPlayNext) {
            return;
        }

        // if playlist finished, we still have the last one selected. We need to move to next first
        if (playlistFinished && playlistManager.canMoveToNext()) {
            next();
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
                List<Playable> newRelated = removeAlreadyInPlaylist(related);
                if (newRelated.size() == 0) {
                    Timber.w("No related found can't auto enqueue");
                    return;
                }
                enqueue(newRelated.get(0), position);
            }
        });
    }

    private List<Playable> removeAlreadyInPlaylist(List<Playable> playables) {
        ArrayList<Playable> newPlayables = new ArrayList<>();
        for (Playable playable: playables) {
            if (getPlaylist().contains(playable)) {
                continue;
            }
            newPlayables.add(playable);
        }
        return newPlayables;
    }

    /**
     * play the current playlist at the given seek value
     * if playlist is empty, that function does nothing
     * @param seekValue
     * @throws EmptyPlaylistException
     */
    public void playAt(final float seekValue) throws EmptyPlaylistException {
        if (playlistManager.size() == 0) {
            throw new EmptyPlaylistException("Can't start an empty playlist");
        }

        autoPlayNext = false;
        playlistFinished = false;

        final Playable playable = playlistManager.getSelected();
        Player player = playable.getPlayer();
        // initialize player if required
        if (!player.isInitialized()) {
            player.initialize(getAutoPlayInitializedListener(seekValue),
                    getDefaultPlayerStoppedListener());
            return;
        }

        // store playable in history
        if (History.getInstance().isInitialized()) {
            History.getInstance().store(playable);
        }

        playable.play();
        if (seekValue != 0) {
            seekTo(seekValue);
        }
    }

    private Player.OnInitializedListener getAutoPlayInitializedListener(final float seekValue) {
        return new Player.OnInitializedListener() {
            @Override
            public void onInitializationSuccess() {
                playAt(seekValue);
            }

            @Override
            public void onInitializationFailure(String reason) {
                Timber.e("Can't play current index: " + reason + ". Moving to next");
                if (!playlistManager.canMoveToNext()) {
                    return;
                }
                next();
            }
        };
    }

    private Player.OnPlayerStoppedListener getDefaultPlayerStoppedListener() {
        return new Player.OnPlayerStoppedListener() {
            @Override
            public void onPlayerStopped() {
                // if we can move to next, do it
                if (playlistManager.canMoveToNext()) {
                    next();
                    return;
                }
                autoPlayNext = true;
                notifyPlaylistFinished();
            }

            @Override
            public void onPlayerError() {
                // if we can move to next, do it
                if (playlistManager.canMoveToNext()) {
                    next();
                    return;
                }
                autoPlayNext = true;
            }
        };
    }

    /**
     * Starts the playback. If playlist is empty, that function does nothing
     * @throws EmptyPlaylistException
     */
    public void play() throws EmptyPlaylistException {
        playAt(0);
    }

    public void pause() throws EmptyPlaylistException {
        if (playlistManager.size() == 0) {
            throw new EmptyPlaylistException("Can't pause an empty playlist");
        }
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

    public boolean canMoveToNext() {
        return playlistManager.canMoveToNext();
    }

    public void next() throws IndexOutOfBoundsException {
        // if it's the last
        if (playlistManager.isEmpty() || !playlistManager.canMoveToNext()) {
            throw new IndexOutOfBoundsException("End of playlist reached");
        }
        pause();
        playlistManager.next();
        play();
    }

    public boolean canMoveToPrevious() {
        return playlistManager.canMoveToPrevious();
    }

    public void previous() throws IndexOutOfBoundsException {
        // if it's the first
        if (playlistManager.isEmpty() || !playlistManager.canMoveToPrevious()) {
            throw new IndexOutOfBoundsException("Start of playlist reached");
        }
        pause();
        playlistManager.previous();
        play();
    }

    // position is a [0, 1] value
    public void seekTo(float position) throws EmptyPlaylistException {
        if (playlistManager.isEmpty()) {
            throw new EmptyPlaylistException("Can't seek on an empty playlist");
        }
        Playable playable = playlistManager.getSelected();
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
        if (!playlistManager.hasIndex(playableIndex)) {
            throw new IndexOutOfBoundsException("Index doesn't exists");
        }
        playlistManager.reorder(playableIndex, position);
    }

    public void remove(int playableIndex) throws IndexOutOfBoundsException {
        if (!playlistManager.hasIndex(playableIndex)) {
            throw new IndexOutOfBoundsException("Index doesn't exists");
        }
        playlistManager.remove(playableIndex);
        if (getPlaylist().size() == 0) {
            autoPlayNext = true;
        }
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
