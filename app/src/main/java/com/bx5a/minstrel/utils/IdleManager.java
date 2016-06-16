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

import java.util.concurrent.Semaphore;

import timber.log.Timber;

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
    private final long GRANULARITY_MILLISECONDS = 1000;
    private final long IDLE_BOUND_MILLISECONDS = 10000;  // 10seconds
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
        lastActivityTime = System.currentTimeMillis();
        isIdle = false;
        inactivityMillisecondsBeforeIdle = IDLE_BOUND_MILLISECONDS;
        stop = false;
        mutex = new Semaphore(1);
        checkerThread = new Thread() {
            @Override
            public void run() {
                while(!stop) {
                    try {
                        mutex.acquire();
                        long idleTime = lastActivityTime + inactivityMillisecondsBeforeIdle;
                        long millisecondBeforeIdle = idleTime - System.currentTimeMillis();
                        if (millisecondBeforeIdle < 0 && !isIdle) {
                            idleEventListener.onIdleStart();
                            isIdle = true;
                        }
                        mutex.release();
                    } catch (InterruptedException e) {
                        Timber.w(e, "Thread did interrupt");
                    }

                    try {
                        Thread.sleep(GRANULARITY_MILLISECONDS);
                    } catch (InterruptedException e) {
                        Timber.w(e, "Can't sleep thread");
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
