package com.bx5a.minstrel.youtube;

import android.content.Context;
import android.util.Log;

import com.bx5a.minstrel.player.Playable;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by guillaume on 24/03/2016.
 */
public class YoutubeVideo implements Playable {
    private String id;
    protected String title;
    private String thumbnailURL;
    private BigInteger viewCount;
    private String duration;

    public static ArrayList<YoutubeVideo> search(Context context, String keywords) {
        // TODO: should search engine be singleton ?
        YoutubeSearchEngine youtubeSearchEngine = new YoutubeSearchEngine(context);
        List<YoutubeVideo> videos = youtubeSearchEngine.search(keywords);
        if (videos == null) {
            return null;
        }

        ArrayList<YoutubeVideo> items = new ArrayList<>();
        for (YoutubeVideo video : videos) {
            items.add(video);
        }
        return items;
    }

    @Override
    public void initFromId(String id, Context context) {
        ArrayList<String> ids = new ArrayList<>();
        ids.add(id);
        YoutubeSearchEngine searchEngine = new YoutubeSearchEngine(context);
        try {
            List<YoutubeVideo> videos = searchEngine.getVideoDetails(ids);
            if (videos.size() != 1) {
                Log.e("YoutubeVideo", "Couldn't initialize from id: search engine error");
                return;
            }
            YoutubeVideo video = videos.get(0);
            setId(id);
            setTitle(video.getTitle());
            setDuration(video.duration);
            setThumbnailURL(video.thumbnailURL);
            setViewCount(video.viewCount);
        } catch (IOException e) {
            Log.e("YoutubeVideo", "Couldn't initialize from id: " + e.getMessage());
        }
    }

    @Override
    public void load() {
        YoutubePlayer.getInstance().load(this);
    }

    @Override
    public boolean isLoaded() {
        String playingId = YoutubePlayer.getInstance().getLoadedId();
        if (playingId.isEmpty()) {
            return false;
        }
        return getId().equals(playingId);
    }

    @Override
    public void play() {
        YoutubePlayer.getInstance().play();
    }

    @Override
    public void pause() {
        YoutubePlayer.getInstance().pause();
    }

    @Override
    public String title() {
        return this.title;
    }

    @Override
    public void seekTo(float position) {
        YoutubePlayer.getInstance().seekTo(position);
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThumbnailURL() {
        return thumbnailURL;
    }

    public void setThumbnailURL(String thumbnailURL) {
        this.thumbnailURL = thumbnailURL;
    }

    public BigInteger getViewCount() {
        return viewCount;
    }

    public void setViewCount(BigInteger viewCount) {
        this.viewCount = viewCount;
    }
}
