package com.bx5a.minstrel.youtube;

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
    }

    public void setYoutubePlayer(YouTubePlayer youtubePlayer) {
        this.youtubePlayer = youtubePlayer;
        this.youtubePlayer.setPlaybackEventListener(new YouTubePlayer.PlaybackEventListener() {
            @Override
            public void onPlaying() {

            }

            @Override
            public void onPaused() {

            }

            @Override
            public void onStopped() {
                // that function is called is an error happened or song finishes
                // TODO: MasterPlayer.getInstance().next(); (+ try/catch)
            }

            @Override
            public void onBuffering(boolean b) {

            }

            @Override
            public void onSeekTo(int i) {

            }
        });
    }

    public void load(YoutubeVideo video) throws IllegalStateException {
        if (youtubePlayer == null) {
            throw new IllegalStateException("Youtube player isn't initialized");
        }
        pause();
        youtubePlayer.loadVideo(video.getId());
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
