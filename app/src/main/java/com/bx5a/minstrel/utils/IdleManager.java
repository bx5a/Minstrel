package com.bx5a.minstrel.utils;

import android.os.Handler;
import android.util.Log;

import java.util.concurrent.Semaphore;

/**
 * Created by guillaume on 27/05/2016.
 */
public class IdleManager {
    private static IdleManager ourInstance = new IdleManager();

    public static IdleManager getInstance() {
        return ourInstance;
    }

    public interface IdleEventListener {
        void onIdleStart();
        void onIdleStop();
    }

    private IdleEventListener idleEventListener;
    private boolean isIdle;
    private long inactivityMillisecondsBeforeIdle;
    private long lastActivityTime;
    private final long kGranularityMilliseconds = 1000;
    private Thread checkerThread;
    private boolean stop;
    private Semaphore mutex;

    private IdleManager() {
        // inits
        idleEventListener = new IdleEventListener() {
            @Override
            public void onIdleStart() {

            }

            @Override
            public void onIdleStop() {

            }
        };
        isIdle = false;
        inactivityMillisecondsBeforeIdle = 60000;  // default is 1minute
        stop = false;
        mutex = new Semaphore(1);
        checkerThread = new Thread() {
            @Override
            public void run() {
                while(!stop) {
                    try {
                        mutex.acquire();
                        if (lastActivityTime + inactivityMillisecondsBeforeIdle < System.currentTimeMillis() && !isIdle) {
                            idleEventListener.onIdleStart();
                            isIdle = true;
                        }
                        mutex.release();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    try {
                        Thread.sleep(kGranularityMilliseconds);
                    } catch (InterruptedException e) {
                        Log.w("IdleManager", "Can't sleep thread: " + e.getMessage());
                    }
                }
            }
        };
        resetIdleTimer();
        checkerThread.start();
    }

    public void setIdleEventListener(IdleEventListener idleEventListener) {
        try {
            mutex.acquire();
            this.idleEventListener = idleEventListener;
            mutex.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setInactivityMillisecondsBeforeIdle(long value) {
        try {
            mutex.acquire();
            inactivityMillisecondsBeforeIdle = value;
            mutex.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resetIdleTimer() {
        boolean wasIdle = false;

        try {
            mutex.acquire();
            lastActivityTime = System.currentTimeMillis();
            wasIdle = isIdle;
            isIdle = false;
            mutex.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (!wasIdle) {
            return;
        }
        idleEventListener.onIdleStop();
    }

    private void stop() {
        try {
            mutex.acquire();
            stop = true;
            mutex.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
