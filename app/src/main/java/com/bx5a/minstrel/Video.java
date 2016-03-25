package com.bx5a.minstrel;

import android.content.Context;

import com.bx5a.minstrel.player.Playable;
import com.bx5a.minstrel.youtube.YoutubeSearchEngine;
import com.bx5a.minstrel.youtube.YoutubeVideo;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by guillaume on 23/03/2016.
 */
public abstract class Video implements Playable {
    private String id;
    protected String title;
    private String thumbnailURL;
    private BigInteger viewCount;
    private String duration;

    public static ArrayList<Video> searchYoutube(Context context, String keywords) {
        // TODO: should be a singleton ?
        YoutubeSearchEngine youtubeSearchEngine = new YoutubeSearchEngine(context);
        List<YoutubeVideo> videos = youtubeSearchEngine.search(keywords);
        if (videos == null) {
            return null;
        }

        ArrayList<Video> items = new ArrayList<Video>();
        for(YoutubeVideo video : videos){
            items.add(video);
        }
        return items;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

