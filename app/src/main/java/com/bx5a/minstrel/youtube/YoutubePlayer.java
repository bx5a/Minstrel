package com.bx5a.minstrel.youtube;

import android.util.Log;

import com.bx5a.minstrel.player.MasterPlayer;
import com.bx5a.minstrel.player.Player;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

import java.util.LinkedList;

/**
 * Created by guillaume on 11/04/2016.
 */
public class YoutubePlayer implements Player {
    private static YoutubePlayer ourInstance = new YoutubePlayer();

    private YouTubePlayer youtubePlayer;
    private String loadedId;
    private YouTubePlayerSupportFragment playerFragment;
    private OnPlayerStoppedListener playerStoppedListener;

    // In youtube API, the load method is asynchronous. Therefore, doing
    // player.load(id);
    // player.seekTo(0.5);
    // would fail because it wouldn't wait until the song is loaded before playing it. To still
    // allow playing right after loading, the PlayerStateChangeListener let us define what we want
    // to do when everything is loaded.
    // In our system we want to be able to queue load and seekTo requests without worrying about the
    // asynchronous system. To do so, we use a TaskQueue: A queue that will execute the next request
    // when it finishes
    // TODO: move the TaskQueue system to utils
    private interface TaskCompletionListener {
        void onTaskCompleted();
    }

    private abstract class Task {
        private TaskCompletionListener completionListener;

        public Task() {
            completionListener = null;
        }

        abstract void start();

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
    private class TaskQueue {
        private LinkedList<Task> tasks;

        public TaskQueue() {
            tasks = new LinkedList<>();
        }

        public void queueTask(Task task) {
            task.setTaskCompletionListener(new TaskCompletionListener() {
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

    private TaskQueue taskQueue;
    private Task loadingTask;

    public static YoutubePlayer getInstance() {
        return ourInstance;
    }

    private YoutubePlayer() {
        youtubePlayer = null;
        loadedId = new String("");
        taskQueue = new TaskQueue();
        loadingTask = null;
        playerStoppedListener = null;
    }

    public String getLoadedId() {
        return loadedId;
    }

    public void reset() {
        MasterPlayer.getInstance().unregisterPlayer(this);
        youtubePlayer = null;
        loadedId = "";
    }

    public void unload() {
        loadedId = "";
    }

    public void setPlayerFragment(YouTubePlayerSupportFragment playerFragment) {
        this.playerFragment = playerFragment;
        // player needs to be reinitialized
        this.youtubePlayer = null;
        this.playerStoppedListener = null;
    }

    public boolean isInitialized() {
        return youtubePlayer != null;
    }

    public void initialize(final OnInitializedListener listener,
                           OnPlayerStoppedListener playerStoppedListener) {
        this.playerStoppedListener = playerStoppedListener;
        playerFragment.initialize(DeveloperKey.DEVELOPER_KEY, new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                setYoutubePlayer(youTubePlayer);
                listener.onInitializationSuccess();
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                listener.onInitializationFailure(youTubeInitializationResult.toString());
            }
        });
    }

    private void setYoutubePlayer(final YouTubePlayer youtubePlayer) {
        this.youtubePlayer = youtubePlayer;
        // hide built in controls player
        this.youtubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);
        this.youtubePlayer.setPlaybackEventListener(new YouTubePlayer.PlaybackEventListener() {
            @Override
            public void onPlaying() {
                MasterPlayer.getInstance().notifyPlayStateChanged();
            }

            @Override
            public void onPaused() {
                MasterPlayer.getInstance().notifyPlayStateChanged();
            }

            @Override
            public void onStopped() {
                MasterPlayer.getInstance().notifyPlayStateChanged();
            }

            @Override
            public void onBuffering(boolean b) {

            }

            @Override
            public void onSeekTo(int i) {

            }
        });

        this.youtubePlayer.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener() {
            @Override
            public void onLoading() {
            }

            @Override
            public void onLoaded(String s) {
                loadedId = s;
            }

            @Override
            public void onAdStarted() {
            }

            @Override
            public void onVideoStarted() {
                if (loadingTask == null) {
                    Log.e("YoutubePlayer", "On loaded called even if loadingTask isn't set");
                    return;
                }
                Task currentTask = loadingTask;
                loadingTask = null;
                currentTask.markAsComplete();
            }

            @Override
            public void onVideoEnded() {
                videoStopped();
            }

            @Override
            public void onError(YouTubePlayer.ErrorReason errorReason) {
                videoStopped();
            }

            private void videoStopped() {
                loadedId = "";
                if (playerStoppedListener != null) {
                    playerStoppedListener.onPlayerStopped();
                }
            }
        });

        MasterPlayer.getInstance().registerPlayer(this);
    }

    public void load(final YoutubeVideo video) throws IllegalStateException {
        if (!isInitialized()) {
            throw new IllegalStateException("Youtube player isn't initialized");
        }
        pause();

        loadingTask = new Task() {
            @Override
            void start() {
                youtubePlayer.loadVideo(video.getId());
                // task will be markedAsComplete by the playerStateChangeListener
            }
        };
        taskQueue.queueTask(loadingTask);
    }

    public void play() throws IllegalStateException {
        if (!isInitialized()) {
            throw new IllegalStateException("Youtube player isn't initialized");
        }
        taskQueue.queueTask(new Task() {
            @Override
            void start() {
                if (!youtubePlayer.isPlaying()) {
                    youtubePlayer.play();
                }
                markAsComplete();
            }
        });
    }

    public void pause() throws IllegalStateException {
        if (!isInitialized()) {
            throw new IllegalStateException("Youtube player isn't initialized");
        }
        taskQueue.queueTask(new Task() {
            @Override
            void start() {
                if (youtubePlayer.isPlaying()) {
                    youtubePlayer.pause();
                }
                markAsComplete();
            }
        });
    }

    public void seekTo(final float position) throws IllegalStateException {
        if (!isInitialized()) {
            throw new IllegalStateException("Youtube player isn't initialized");
        }
        taskQueue.queueTask(new Task() {
            @Override
            void start() {
                int millis = (int) (position * youtubePlayer.getDurationMillis());
                youtubePlayer.seekToMillis(millis);
                markAsComplete();
            }
        });
    }

    public float getCurrentPosition() {
        if (!isInitialized()) {
            return 0;
        }
        return (float)(youtubePlayer.getCurrentTimeMillis()) / youtubePlayer.getDurationMillis();
    }

    public boolean isPlaying() {
        if (!isInitialized()) {
            return false;
        }
       return youtubePlayer.isPlaying();
    }
}
