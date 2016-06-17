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

import android.view.MotionEvent;
import android.view.View;

/**
 * Decode a activity touchEvents into view click events
 * usage:
 * Add that handler in an activity and initialize it with the desired event listener. To enable it,
 * override the dispatchTouchEvent function like:
 * @Override
 * public boolean dispatchTouchEvent(MotionEvent ev) {
 *   motionEventHandler.dispatchEvent(ev);
 *   return super.dispatchTouchEvent(ev);
 * }
 * the motionEventHandler will decode the MotionEvents and trigger the event as required
 */
public class MotionEventHandler {
    private View view;
    private View.OnClickListener listener;
    boolean isPressing;

    public MotionEventHandler() {
        view = null;
        listener = null;
        isPressing = false;
    }

    public void setOnViewClickListener(View view, View.OnClickListener listener) {
        this.view = view;
        this.listener = listener;
    }

    private boolean isInitialized() {
        return view != null && listener != null;
    }

    public void dispatchEvent(MotionEvent ev) {
        if (!isInitialized()) {
            return;
        }

        if (!eventIsOnView(ev, view)) {
            return;
        }

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onActionDown();
                break;
            case MotionEvent.ACTION_UP:
                onActionUp();
                break;
            default:
                onActionDefault();
                break;
        }
    }

    private void onActionDown() {
        isPressing = true;
    }

    private void onActionUp() {
        if (isPressing) {
            listener.onClick(view);
        }
        isPressing = false;
    }

    private void onActionDefault() {
        // dismiss press
        isPressing = false;
    }

    private boolean eventIsOnView(MotionEvent event, View view) {
        float x = event.getX();
        float y = event.getY();

        int[] positions = new int[2];
        view.getLocationOnScreen(positions);

        float viewX = positions[0];
        float viewY = positions[1];
        float viewHeight = view.getHeight();
        float viewWidth = view.getWidth();

        return (x >= viewX
                && x <= viewX + viewWidth
                && y >= viewY
                && y <= viewY + viewHeight);
    }
}
