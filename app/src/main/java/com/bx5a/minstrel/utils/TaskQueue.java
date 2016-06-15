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

import java.util.LinkedList;

/**
 * Queue of task that automatically start contained tasks if none is running or if completion of
 * previous task is detected
 */
public class TaskQueue {
    private LinkedList<Task> tasks;

    public TaskQueue() {
        tasks = new LinkedList<>();
    }

    public void queueTask(Task task) {
        task.setTaskCompletionListener(new Task.TaskCompletionListener() {
            @Override
            public void onTaskCompleted() {
                // remove the completed task
                tasks.poll();
                startNextTask();
            }
        });
        tasks.add(task);

        // if we only have the task we've just added, we start it
        if (tasks.size() == 1) {
            startNextTask();
        }
    }

    private void startNextTask() {
        if (tasks.size() == 0) {
            return;
        }
        tasks.getFirst().start();
    }
}
