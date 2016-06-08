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

/**
 * Generic definition of an audio player. Should be implemented for each streaming service
 */
public interface Player {
    interface OnInitializedListener {
        void onInitializationSuccess();
        void onInitializationFailure(String reason);
    }
    interface OnPlayerStoppedListener {
        void onPlayerStopped();
        void onPlayerError();
    }

    void initialize(OnInitializedListener listener, OnPlayerStoppedListener playerStoppedListener);
    boolean isInitialized();

    void play();
    void pause();
    boolean isPlaying();

    /**
     * Seek on the currently playing song.
     * @param position is a value in the [0, 1] interval
     */
    void seekTo(float position);
    float getCurrentPosition();

    void unload();
}
