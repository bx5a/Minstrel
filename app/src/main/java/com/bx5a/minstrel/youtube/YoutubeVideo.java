package com.bx5a.minstrel.youtube;

import android.os.AsyncTask;
import android.util.Log;

import com.bx5a.minstrel.player.Playable;
import com.bx5a.minstrel.player.Player;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by guillaume on 24/03/2016.
 */
public class YoutubeVideo implements Playable {
    private String id;
    protected String title;
    private String thumbnailURL;
    private BigInteger viewCount;
    private String duration;

    public static ArrayList<YoutubeVideo> search(String keywords) {
        // TODO: should search engine be singleton ?
        YoutubeSearchEngine youtubeSearchEngine = YoutubeSearchEngine.getInstance();
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
    public void initFromId(String id) throws IOException {
        ArrayList<String> ids = new ArrayList<>();
        ids.add(id);
        YoutubeSearchEngine searchEngine = YoutubeSearchEngine.getInstance();
        List<YoutubeVideo> videos = searchEngine.getVideoDetails(ids);
        if (videos.size() != 1) {
            throw new IOException("Couldn't initialize from id: search engine error");
        }
        YoutubeVideo video = videos.get(0);
        setId(id);
        setTitle(video.getTitle());
        setDuration(video.duration);
        setThumbnailURL(video.thumbnailURL);
        setViewCount(video.viewCount);
    }

    @Override
    public void load() throws IllegalStateException {
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
    public void play() throws IllegalStateException {
        YoutubePlayer.getInstance().play();
    }

    @Override
    public void pause() throws IllegalStateException {
        YoutubePlayer.getInstance().pause();
    }

    @Override
    public String title() {
        return this.title;
    }

    @Override
    public void seekTo(float position) throws IllegalStateException {
        YoutubePlayer.getInstance().seekTo(position);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getThumbnailURL() {
        return thumbnailURL;
    }

    @Override
    public String duration() {
        // youtube duration is ISO 8601 string (format is PT[MINUTES]M[SECONDS]S)
        Pattern pattern = Pattern.compile("PT(.*)M(.*)S");
        Matcher matcher = pattern.matcher(getDuration());
        if (matcher.matches()) {
            String seconds = matcher.group(2);
            if (seconds.length() == 1) {
                seconds = "0" + seconds;
            }
            return matcher.group(1) + ":" + seconds;
        }
        return duration;
    }

    @Override
    public Player getPlayer() {
        return YoutubePlayer.getInstance();
    }

    @Override
    public void asyncGetRelated(RelatedAvailableListener eventListener) {
        new AsyncGetRelated(eventListener).execute(this);
    }

    public String getDuration() {
        return duration;
    }

    public void setId(String id) {
        this.id = id;
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

    public void setThumbnailURL(String thumbnailURL) {
        this.thumbnailURL = thumbnailURL;
    }

    public BigInteger getViewCount() {
        return viewCount;
    }

    public void setViewCount(BigInteger viewCount) {
        this.viewCount = viewCount;
    }

    private class AsyncGetRelated extends AsyncTask<YoutubeVideo, Integer, List<Playable>> {
        private RelatedAvailableListener eventListener;

        public AsyncGetRelated(RelatedAvailableListener eventListener) {
            this.eventListener = eventListener;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<Playable> doInBackground(YoutubeVideo... params) {
            try {
                List<String> relatedIds =
                        YoutubeSearchEngine.getInstance().relatedVideoIds(params[0]);
                ArrayList<Playable> result = new ArrayList<>();
                for (String id : relatedIds) {
                    YoutubeVideo related = new YoutubeVideo();
                    related.initFromId(id);
                    result.add(related);
                }
                return result;
            } catch (IOException e) {
                Log.w("YoutubeVideo", "Couldn't retrieve related: " + e.getMessage());
                return new ArrayList<>();
            }
        }

        @Override
        protected void onPostExecute(List<Playable> relatedList) {
            super.onPostExecute(relatedList);
            eventListener.onRelatedAvailable(relatedList);
        }
    }
}
