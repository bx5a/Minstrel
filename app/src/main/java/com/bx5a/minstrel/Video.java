package com.bx5a.minstrel;

import android.content.Context;

import com.bx5a.minstrel.youtube.YoutubeSearchEngine;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;

import java.util.ArrayList;

/**
 * Created by guillaume on 23/03/2016.
 */
public class Video {
    private String title;
    private String description;
    private String thumbnailURL;
    private String id;

    public static ArrayList<Video> searchYoutube(Context context, String keywords) {
        // TODO: should be a singleton ?
        YoutubeSearchEngine youtubeSearchEngine = new YoutubeSearchEngine(context);
        SearchListResponse results = youtubeSearchEngine.search(keywords);
        if (results == null) {
            return null;
        }

        ArrayList<Video> items = new ArrayList<Video>();
        for(SearchResult result:results.getItems()){
            Video item = new Video();
            item.setTitle(result.getSnippet().getTitle());
            item.setDescription(result.getSnippet().getDescription());
            item.setThumbnailURL(result.getSnippet().getThumbnails().getDefault().getUrl());
            item.setId(result.getId().getVideoId());
            items.add(item);
        }
        return items;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

