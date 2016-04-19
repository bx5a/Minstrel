package com.bx5a.minstrel.player;

import android.app.IntentService;
import android.content.Intent;

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
            Intent notificationIntent = new Intent("Minstrel.currentTimeChanged");
            sendBroadcast(notificationIntent);

            try {
                Thread.sleep(1000);  // every seconds is enough
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
