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

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.bx5a.minstrel.R;

import static android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences;

public class LowBrightnessOnIdleActivity  extends AppCompatActivity {
    private final int kIdleTimeMilliseconds = 5000;  // 5 seconds

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
                        if (!getDefaultSharedPreferences(getBaseContext()).getBoolean("brightness_management",
                                getDefaultPreferencesValue(R.bool.brightness_management_default))) {
                            return;
                        }
                        WindowManager.LayoutParams layoutParameters = getWindow().getAttributes();
                        layoutParameters.screenBrightness =
                                WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF;
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
                        layoutParameters.screenBrightness =
                                WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
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

    protected boolean getDefaultPreferencesValue(int resid) {
        return getResources().getBoolean(resid);
    }
}
