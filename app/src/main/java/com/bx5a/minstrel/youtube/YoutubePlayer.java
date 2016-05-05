package com.bx5a.minstrel.youtube;

import android.util.Log;

import com.bx5a.minstrel.player.MasterPlayer;
import com.bx5a.minstrel.player.Player;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

/**
 * Created by guillaume on 11/04/2016.
 */
public class YoutubePlayer implements Player {
    private static YoutubePlayer ourInstance = new YoutubePlayer();

    private YouTubePlayer youtubePlayer;
    private String loadedId;
    private YouTubePlayerSupportFragment playerFragment;

    public static YoutubePlayer getInstance() {
        return ourInstance;
    }

    private YoutubePlayer() {
        youtubePlayer = null;
        loadedId = new String("");
    }

    public String getLoadedId() {
        return loadedId;
    }

    public void reset() {
        MasterPlayer.getInstance().unregisterPlayer(this);
        youtubePlayer = null;
        loadedId = new String("");
    }

    public void setPlayerFragment(YouTubePlayerSupportFragment playerFragment) {
        this.playerFragment = playerFragment;
    }

    public boolean isInitialized() {
        return youtubePlayer != null;
    }

    public void initialize(final OnInitializedListener listener) {
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
                youtubePlayer.play();
                loadedId = s;
            }

            @Override
            public void onAdStarted() {
            }

            @Override
            public void onVideoStarted() {
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
                try {
                    MasterPlayer.getInstance().next();
                } catch (IndexOutOfBoundsException exception) {
                    Log.i("YoutubePlayer", "Couldn't move to next song: " + exception.getMessage());
                }
            }
        });

        MasterPlayer.getInstance().registerPlayer(this);
    }

    public void load(YoutubeVideo video) throws IllegalStateException {
        if (!isInitialized()) {
            throw new IllegalStateException("Youtube player isn't initialized");
        }
        pause();
        youtubePlayer.loadVideo(video.getId());
    }

    public void play() throws IllegalStateException {
        if (!isInitialized()) {
            throw new IllegalStateException("Youtube player isn't initialized");
        }
        youtubePlayer.play();
    }

    public void pause() throws IllegalStateException {
        if (!isInitialized()) {
            throw new IllegalStateException("Youtube player isn't initialized");
        }
        youtubePlayer.pause();
    }

    public void seekTo(float position) throws IllegalStateException {
        if (!isInitialized()) {
            throw new IllegalStateException("Youtube player isn't initialized");
        }
        int millis = (int) (position * youtubePlayer.getDurationMillis());
        youtubePlayer.seekToMillis(millis);
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
