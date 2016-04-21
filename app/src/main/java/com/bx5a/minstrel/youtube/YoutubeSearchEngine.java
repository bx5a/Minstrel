package com.bx5a.minstrel.youtube;

import android.content.Context;

import com.bx5a.minstrel.R;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Joiner;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    private List<String> searchVideoIds(String keywords) throws IOException {
        YouTube.Search.List query = youtube.search().list("id");
        query.setKey(DeveloperKey.DEVELOPER_KEY);
        query.setType("video");
        query.setFields("items(id/videoId)");
        query.setQ(keywords);
        SearchListResponse response = query.execute();

        List<String> ids = new ArrayList<>();
        for (SearchResult result : response.getItems()) {
            ids.add(result.getId().getVideoId());
        }
        return ids;
    }

    public List<YoutubeVideo> getVideoDetails(List<String> videoIds) throws IOException {
        Joiner stringJoiner = Joiner.on(',');
        String videoId = stringJoiner.join(videoIds);
        YouTube.Videos.List query =
                youtube.videos().list("contentDetails,snippet,statistics").setId(videoId);
        query.setKey(DeveloperKey.DEVELOPER_KEY);
        query.setFields("items(id,snippet/title,snippet/thumbnails/default/url," +
                "contentDetails/duration,statistics/viewCount)");

        VideoListResponse response = query.execute();

        List<YoutubeVideo> youtubeVideos = new ArrayList<>();
        for (Video video : response.getItems()) {
            YoutubeVideo youtubeVideo = new YoutubeVideo();

            youtubeVideo.setTitle(video.getSnippet().getTitle());
            youtubeVideo.setThumbnailURL(video.getSnippet().getThumbnails().getDefault().getUrl());
            youtubeVideo.setId(video.getId());
            youtubeVideo.setDuration(video.getContentDetails().getDuration());
            youtubeVideo.setViewCount(video.getStatistics().getViewCount());

            youtubeVideos.add(youtubeVideo);
        }

        return youtubeVideos;
    }

    public List<YoutubeVideo> search(String keywords) {
        // search with given keyword
        List<String> videoIds;
        try {
            videoIds = searchVideoIds(keywords);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        // for each answer, search for details
        List<YoutubeVideo> videos;
        try {
            videos = getVideoDetails(videoIds);
            return videos;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }
}
