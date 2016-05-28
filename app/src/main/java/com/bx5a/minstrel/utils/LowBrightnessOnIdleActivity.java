package com.bx5a.minstrel.utils;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

/**
 * Created by guillaume on 27/05/2016.
 */
public class LowBrightnessOnIdleActivity  extends AppCompatActivity {
    private final int kIdleTimeMilliseconds = 5000;  // 5 seconds
    private float beforeIdleBrightness;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initIdleManager();
    }

    private void initIdleManager() {
        final Handler mainHandler = new Handler(getBaseContext().getMainLooper());
        IdleManager idleManager = IdleManager.getInstance();
        idleManager.setInactivityMillisecondsBeforeIdle(kIdleTimeMilliseconds);
        idleManager.setIdleEventListener(new IdleManager.IdleEventListener() {
            @Override
            public void onIdleStart() {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        WindowManager.LayoutParams layoutParameters = getWindow().getAttributes();
                        beforeIdleBrightness = layoutParameters.screenBrightness;
                        layoutParameters.screenBrightness = 0;
                        getWindow().setAttributes(layoutParameters);
                    }
                });
            }

            @Override
            public void onIdleStop() {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        WindowManager.LayoutParams layoutParameters = getWindow().getAttributes();
                        layoutParameters.screenBrightness = beforeIdleBrightness;
                        getWindow().setAttributes(layoutParameters);
                    }
                });
            }
        });
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        IdleManager.getInstance().resetIdleTimer();
    }

    @Override
    public void onPause() {
        super.onPause();
        IdleManager.getInstance().resetIdleTimer();
    }
}
