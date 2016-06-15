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

package com.bx5a.minstrel.youtube;

import com.bx5a.minstrel.player.MasterPlayer;
import com.bx5a.minstrel.player.Player;
import com.bx5a.minstrel.utils.Task;
import com.bx5a.minstrel.utils.TaskQueue;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

import timber.log.Timber;

/**
 * Implementation of the Player interface for the YouTube streaming service
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
                    Timber.e("On loaded called even if loadingTask isn't set");
                    return;
                }
                markLoadingTaskAsComplete();
            }

            @Override
            public void onVideoEnded() {
                loadedId = "";
                if (playerStoppedListener != null) {
                    playerStoppedListener.onPlayerStopped();
                }
            }

            @Override
            public void onError(YouTubePlayer.ErrorReason errorReason) {
                // TODO: pop error info ??
                // if we are still in the loading process. We can say that it failed
                if (loadingTask != null) {
                    markLoadingTaskAsComplete();
                }
                loadedId = "";
                if (playerStoppedListener != null) {
                    playerStoppedListener.onPlayerError();
                }
            }
        });

        MasterPlayer.getInstance().registerPlayer(this);
    }

    private void markLoadingTaskAsComplete() {
        Task currentTask = loadingTask;
        loadingTask = null;
        currentTask.markAsComplete();
    }

    public void load(final YoutubeVideo video) throws IllegalStateException {
        if (!isInitialized()) {
            throw new IllegalStateException("Youtube player isn't initialized");
        }
        pause();

        // if a current loading task is pending, mark it as finished as we won't wait for its
        // completion anyway
        if (loadingTask != null) {
            markLoadingTaskAsComplete();
        }

        loadingTask = new Task() {
            @Override
            public void start() {
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
            public void start() {
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
            public void start() {
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
            public void start() {
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
