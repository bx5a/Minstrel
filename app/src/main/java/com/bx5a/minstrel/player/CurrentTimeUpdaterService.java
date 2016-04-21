package com.bx5a.minstrel.player;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

/**
 * Created by guillaume on 19/04/2016.
 */
public class CurrentTimeUpdaterService extends IntentService {
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
                Thread.sleep(1000);  // every seconds is enough
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
