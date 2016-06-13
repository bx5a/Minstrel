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

import android.content.Context;
import android.util.Log;

import com.bx5a.minstrel.R;
import com.bx5a.minstrel.exception.CategoryNotFoundException;
import com.bx5a.minstrel.exception.NotInitializedException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Joiner;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoCategory;
import com.google.api.services.youtube.model.VideoCategoryListResponse;
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
 * Search engine for YouTube
 */
public class YoutubeSearchEngine {
    private YouTube youtube;
    private String countryCode;
    private final long MAX_RESULT_NUMBER = 15;
    private final String MUSIC_CATEGORY_TITLE = "Music";
    private final String DEFAULT_CATEGORY_ID = "0";

    private static YoutubeSearchEngine ourInstance = new YoutubeSearchEngine();

    public static YoutubeSearchEngine getInstance() {
        return ourInstance;
    }

    private YoutubeSearchEngine() {
        youtube = null;
    }

    public boolean isInitialized() {
        return youtube != null;
    }

    /**
     * To operate properly this singleton needs to be initialized. If not,
     * @param context
     */
    public void init(Context context) {
        countryCode = context.getResources().getConfiguration().locale.getCountry();
        youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(),
                new HttpRequestInitializer() {
                    @Override
                    public void initialize(HttpRequest request) throws IOException {

                    }
                }).setApplicationName(context.getString(R.string.app_name)).build();
    }

    public List<String> relatedVideoIds(YoutubeVideo video)
            throws IOException, NotInitializedException {
        if (youtube == null) {
            throw new NotInitializedException("YoutubeSearchEngine's uninitialized");
        }
        SearchList list = new SearchList(youtube, countryCode, getMusicCategoryId());
        list.setRelatedToVideoId(video.getId());
        return list.execute();
    }

    public List<YoutubeVideo> getVideoDetails(List<String> videoIds)
            throws IOException, NotInitializedException {
        if (youtube == null) {
            throw new NotInitializedException("YoutubeSearchEngine's uninitialized");
        }
        Joiner stringJoiner = Joiner.on(',');
        String videoId = stringJoiner.join(videoIds);

        VideoList list = new VideoList(youtube, countryCode, getMusicCategoryId());
        list.setId(videoId);
        return list.execute();
    }

    public List<YoutubeVideo> getPopularVideos() throws IOException, NotInitializedException {
        if (youtube == null) {
            throw new NotInitializedException("YoutubeSearchEngine not initialized");
        }
        VideoList list = new VideoList(youtube, countryCode, getMusicCategoryId());
        list.setChart("mostPopular");
        return list.execute();
    }

    public List<YoutubeVideo> search(String keywords) throws IOException, NotInitializedException {
        if (youtube == null) {
            throw new NotInitializedException("YoutubeSearchEngine not initialized");
        }

        try {
            List<String> suggestedKeywords = autoComplete(keywords);
            if (suggestedKeywords.size() > 0) {
                keywords = suggestedKeywords.get(0);
            }
        } catch (JSONException e) {
            Log.e("YoutubeSearchEngine",
                    "Can't auto complete: " + e.getMessage() + " Defaulting to normal query");
        }

        SearchList list = new SearchList(youtube, countryCode, getMusicCategoryId());
        list.setQ(keywords);
        return getVideoDetails(list.execute());
    }

    /**
     * Gives a list of suggested research using the given keyword
     * @param keyword
     * @return
     */
    private List<String> autoComplete(String keyword) throws IOException, JSONException {
        ArrayList<String> suggestions = new ArrayList<>();

        String path = "complete/search";
        String query = "client=firefox&ds=yt&q=" + keyword.replace(" ", "+");

        URL suggestionApiUrl = new URL("http", "suggestqueries.google.com", path + "?" + query);

        // open a connection to the autocompletion engine
        HttpURLConnection connection = (HttpURLConnection) suggestionApiUrl.openConnection();

        String readQuery = "";
        try {
            InputStream in = new BufferedInputStream(connection.getInputStream());

            // read
            InputStreamReader reader = new InputStreamReader(in);
            int c;
            while ((c = reader.read()) != -1) {
                readQuery = readQuery + (char) c;
            }
        } catch (IOException e) {
            throw e;
        } finally {
            // Force the closing of the connection
            connection.disconnect();
        }

        // response is an array of size 2: 0 = keyword / 1 = array of response
        JSONArray array = new JSONArray(readQuery).getJSONArray(1);
        for (int index = 0; index < array.length(); index++) {
            suggestions.add(array.getString(index));
        }

        return suggestions;
    }

    private String getMusicCategoryId() {
        try {
            return getCategoryIdFromTitle(MUSIC_CATEGORY_TITLE);
        } catch (CategoryNotFoundException | IOException e) {
            Log.e("YoutubeSearchEngine",
                    "Couldn't get music category id. Defaulting to " + DEFAULT_CATEGORY_ID);
            return DEFAULT_CATEGORY_ID;
        }
    }

    private String getCategoryIdFromTitle(String categoryTitle)
            throws IOException, CategoryNotFoundException {
        // TODO: cache result instead of query each time
        YouTube.VideoCategories.List query = youtube.videoCategories().list("id,snippet");
        query.setKey(DeveloperKey.DEVELOPER_KEY);
        query.setRegionCode(countryCode);
        query.setFields("items(id,snippet/title)");
        VideoCategoryListResponse response = query.execute();
        for (VideoCategory category : response.getItems()) {
            if (category.getSnippet().getTitle().equals(categoryTitle)) {
                return category.getId();
            }
        }
        throw new CategoryNotFoundException("Category " + categoryTitle + " couldn't be found");
    }

    // Helpers
    private class SearchList {
        private YouTube.Search.List query;
        public SearchList(YouTube youtube, String countryCode, String categoryId)
                throws IOException {
            query = youtube.search().list("id");
            query.setKey(DeveloperKey.DEVELOPER_KEY);
            query.setType("video");
            query.setFields("items(id/videoId)");
            query.setMaxResults(MAX_RESULT_NUMBER);
            query.setRegionCode(countryCode);
            query.setVideoCategoryId(categoryId);
        }
        public void setQ(String q) {
            query.setQ(q);
        }
        public void setRelatedToVideoId(String relatedToVideoId) {
            query.setRelatedToVideoId(relatedToVideoId);
        }
        public List<String> execute() throws IOException {
            SearchListResponse response = query.execute();

            List<String> ids = new ArrayList<>();
            for (SearchResult result : response.getItems()) {
                ids.add(result.getId().getVideoId());
            }
            return ids;
        }
    }

    private class VideoList {
        private YouTube.Videos.List query;
        public VideoList(YouTube youtube, String countryCode, String categoryId)
                throws IOException {
            query = youtube.videos().list("contentDetails,snippet,statistics");
            query.setKey(DeveloperKey.DEVELOPER_KEY);
            query.setFields("items(id,snippet/title,snippet/thumbnails/default/url," +
                    "contentDetails/duration,statistics/viewCount)");
            query.setMaxResults(MAX_RESULT_NUMBER);
            query.setRegionCode(countryCode);
            query.setVideoCategoryId(categoryId);
        }
        public void setChart(String chart) {
            query.setChart(chart);
        }
        public void setId(String id) {
            query.setId(id);
        }
        public List<YoutubeVideo> execute() throws IOException {
            VideoListResponse response = query.execute();

            List<YoutubeVideo> youtubeVideos = new ArrayList<>();
            for (Video video : response.getItems()) {
                YoutubeVideo youtubeVideo = new YoutubeVideo();

                youtubeVideo.setTitle(video.getSnippet().getTitle());
                youtubeVideo.setThumbnailURL(
                        video.getSnippet().getThumbnails().getDefault().getUrl());
                youtubeVideo.setId(video.getId());
                youtubeVideo.setDuration(video.getContentDetails().getDuration());
                youtubeVideo.setViewCount(video.getStatistics().getViewCount());

                youtubeVideos.add(youtubeVideo);
            }

            return youtubeVideos;
        }
    }
}
