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

/**
 * Abstract definition of a task. The start contains the job to be done. When the job finishes, the
 * maskAsComplete needs to be called or the task will never finish and the TaskQueue will never move
 * to the next task
 */
public abstract class Task {
    interface TaskCompletionListener {
        void onTaskCompleted();
    }

    private TaskCompletionListener completionListener;

    public Task() {
        completionListener = null;
    }

    public abstract void start();

    public void markAsComplete() {
        if (completionListener == null) {
            return;
        }
        completionListener.onTaskCompleted();
    }

    public void setTaskCompletionListener(TaskCompletionListener listener) {
        completionListener = listener;
    }
}
