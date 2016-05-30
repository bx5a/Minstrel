package com.bx5a.minstrel.youtube;

import android.content.Context;
import android.util.Log;

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

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by guillaume on 23/03/2016.
 */
public class YoutubeSearchEngine {
    private YouTube youtube;
    private final long kMaxResultNumber = 15;

    public YoutubeSearchEngine(Context context) {
        youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(),
                new HttpRequestInitializer() {
                    @Override
                    public void initialize(HttpRequest request) throws IOException {

                    }
                }).setApplicationName(context.getString(R.string.app_name)).build();
    }

    private YouTube.Search.List initIdQuery() throws IOException {
        YouTube.Search.List query = youtube.search().list("id");
        query.setKey(DeveloperKey.DEVELOPER_KEY);
        query.setType("video");
        query.setFields("items(id/videoId)");
        query.setMaxResults(kMaxResultNumber);
        return query;
    }

    private List<String> executeIdQuery(YouTube.Search.List query) throws IOException {
        SearchListResponse response = query.execute();

        List<String> ids = new ArrayList<>();
        for (SearchResult result : response.getItems()) {
            ids.add(result.getId().getVideoId());
        }
        return ids;
    }

    private List<String> searchVideoIds(String keywords) throws IOException {
        YouTube.Search.List query = initIdQuery();
        query.setQ(keywords);
        return executeIdQuery(query);
    }

    public List<String> relatedVideoIds(YoutubeVideo video) throws IOException {
        YouTube.Search.List query = initIdQuery();
        query.setRelatedToVideoId(video.getId());
        return executeIdQuery(query);
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
        // update keywords using highest score suggested one
        List<String> suggestedKeywords = autoComplete(keywords);
        if (suggestedKeywords.size() != 0) {
            keywords = suggestedKeywords.get(0);
        }

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

    // suggestion api
    private List<String> autoComplete(String keyword) {
        ArrayList<String> suggestions = new ArrayList<>();

        String path = "complete/search";
        String query = "client=firefox&ds=yt&q=" + keyword.replace(" ", "+");

        try {
            URL suggestionApiUrl = new URL("http", "suggestqueries.google.com", path + "?" + query);

            // open a connection to the autocompletion engine
            HttpURLConnection connection = (HttpURLConnection) suggestionApiUrl.openConnection();
            InputStream in = new BufferedInputStream(connection.getInputStream());

            // read
            InputStreamReader reader = new InputStreamReader(in);
            int c;
            String readQuery = "";
            while ((c = reader.read()) != -1) {
                readQuery = readQuery + (char)c;
            }

            // response is an array of size 2: 0 = keyword / 1 = array of response
            JSONArray array = new JSONArray(readQuery).getJSONArray(1);
            for (int index = 0; index < array.length(); index++) {
                suggestions.add(array.getString(index));
            }

            connection.disconnect();
        } catch (IOException e) {
            Log.w("YoutubeSearchEngine", "Can't autocomplete " + keyword + " : " + e.getMessage());
        } catch (JSONException e) {
            Log.w("YoutubeSearchEngine", "Can't parse json for keyword " + keyword + " : " + e.getMessage());
        }

        return suggestions;
    }
}
