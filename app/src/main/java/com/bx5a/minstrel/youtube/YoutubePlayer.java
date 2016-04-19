package com.bx5a.minstrel.youtube;

import android.util.Log;

import com.bx5a.minstrel.player.MasterPlayer;
import com.bx5a.minstrel.player.Player;
import com.google.android.youtube.player.YouTubePlayer;

/**
 * Created by guillaume on 11/04/2016.
 */
public class YoutubePlayer implements Player {
    private static YoutubePlayer ourInstance = new YoutubePlayer();

    private YouTubePlayer youtubePlayer;
    public static YoutubePlayer getInstance() {
        return ourInstance;
    }

    private YoutubePlayer() {
        youtubePlayer = null;
        MasterPlayer.getInstance().registerPlayer(this);
    }

    public void setYoutubePlayer(final YouTubePlayer youtubePlayer) {
        this.youtubePlayer = youtubePlayer;
        // hide built in controls player
        // this.youtubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);
        this.youtubePlayer.setPlaybackEventListener(new YouTubePlayer.PlaybackEventListener() {
            @Override
            public void onPlaying() {
                Log.i("YoutubePlayer", "On Playing... ");
            }

            @Override
            public void onPaused() {
                Log.i("YoutubePlayer", "On Paused... ");
            }

            @Override
            public void onStopped() {
                Log.i("YoutubePlayer", "On Stopped... ");
            }

            @Override
            public void onBuffering(boolean b) {
                Log.i("YoutubePlayer", "On Buffering... ");
            }

            @Override
            public void onSeekTo(int i) {
                Log.i("YoutubePlayer", "On SeekTo... ");
            }
        });

        this.youtubePlayer.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener() {
            @Override
            public void onLoading() {
                Log.i("YoutubePlayer", "Loading... ");
            }

            @Override
            public void onLoaded(String s) {
                Log.i("YoutubePlayer", "Loaded... ");
            }

            @Override
            public void onAdStarted() {
                Log.i("YoutubePlayer", "Ad Started !");
            }

            @Override
            public void onVideoStarted() {
                Log.i("YoutubePlayer", "Started... ");
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
                Log.i("YoutubePlayer", "Stopped... ");
                try {
                    MasterPlayer.getInstance().next();
                } catch (IndexOutOfBoundsException exception) {
                    Log.i("YoutubePlayer", "Couldn't move to next song: " + exception.getMessage());
                }
            }
        });
    }

    public void load(YoutubeVideo video) throws IllegalStateException {
        if (youtubePlayer == null) {
            throw new IllegalStateException("Youtube player isn't initialized");
        }
        pause();
        youtubePlayer.cueVideo(video.getId());
    }

    public void play() {
        youtubePlayer.play();
    }

    public void pause() {
        youtubePlayer.pause();
    }

    public void resume() {
        youtubePlayer.play();
    }

    public void seekTo(float position) {
        int millis = (int) (position * youtubePlayer.getDurationMillis());
        youtubePlayer.seekToMillis(millis);
    }

    public boolean isPlaying() {
       return youtubePlayer.isPlaying();
    }
}
