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

package com.bx5a.minstrel.utils;

import android.os.Handler;
import android.os.Looper;

import com.bx5a.minstrel.player.Player;

import timber.log.Timber;

/**
 * Created by guillaume on 20/06/2016.
 */
public class TimeTrackingTask {
    private int REQUEST_INTERVAL_MILLISECOND = 500;  // 1/2 second

    private Player.OnCurrentTimeChangeListener eventListener;
    private Player player;
    private Runner currentRunner;

    public TimeTrackingTask(Player handledPlayer) {
        player = handledPlayer;
        currentRunner = null;
    }

    public void setOnCurrentTimeChangeListener(Player.OnCurrentTimeChangeListener listener) {
        eventListener = listener;
    }

    public void start() {
        // if already running, don't stop
        if (currentRunner != null && currentRunner.isRunning()) {
            return;
        }
        currentRunner = new Runner();
        currentRunner.start();
    }

    public void stop() {
        if (currentRunner == null) {
            return;
        }
        currentRunner.terminate();
        currentRunner = null;
    }

    private class Runner extends Thread {
        private boolean isStopped;
        public Runner() {
            isStopped = false;
        }

        @Override
        public void run() {
            while (!isStopped) {
                try {
                    Thread.sleep(REQUEST_INTERVAL_MILLISECOND);
                } catch (InterruptedException e) {
                    Timber.e(e, "Can't sleep thread");
                }

                if (eventListener == null) {
                    continue;
                }
                // on main thread
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        eventListener.onCurrentTimeChanged(player.getCurrentPosition());
                    }
                });
            }
        }

        public void terminate() {
            isStopped = true;
        }

        public boolean isRunning() {
            return !isStopped;
        }
    }
}
