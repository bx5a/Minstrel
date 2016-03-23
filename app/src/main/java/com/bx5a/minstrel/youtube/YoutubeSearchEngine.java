package com.bx5a.minstrel.youtube;

import android.content.Context;

import com.bx5a.minstrel.R;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;

import java.io.IOException;

/**
 * Created by guillaume on 23/03/2016.
 */
public class YoutubeSearchEngine {
    private YouTube youtube;

    public YoutubeSearchEngine(Context context) {
        youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(),
                new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest request) throws IOException {

            }
        }).setApplicationName(context.getString(R.string.app_name)).build();
    }

    public SearchListResponse search(String keywords) {
        YouTube.Search.List query;
        try {
            query = youtube.search().list("id,snippet");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        query.setKey(DeveloperKey.DEVELOPER_KEY);
        query.setType("video");
        query.setFields("items(id/videoId,snippet/title," +
                "snippet/description,snippet/thumbnails/default/url)");
        query.setQ(keywords);

        SearchListResponse response;
        try {
            response = query.execute();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return response;
    }
}
