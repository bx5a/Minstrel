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

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

/**
 * Service that force the master player to notify that the current position in song is changing
 */
public class CurrentTimeUpdaterService extends IntentService {
    private int REQUEST_INTERVAL_MILLISECOND = 1000;  // one second

    public CurrentTimeUpdaterService() {
        super("CurrentTimeUpdaterService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        while (true) {
            // if player isn't playing, time didn't update
            // on main thread
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    // notify
                    if (MasterPlayer.getInstance().isPlaying()) {
                        MasterPlayer.getInstance().notifyCurrentTimeChanged();
                    }
                }
            });
            try {
                Thread.sleep(REQUEST_INTERVAL_MILLISECOND);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
