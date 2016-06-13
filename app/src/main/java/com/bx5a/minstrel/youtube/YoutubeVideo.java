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

import android.os.AsyncTask;
import android.util.Log;

import com.bx5a.minstrel.exception.PlayableCreationException;
import com.bx5a.minstrel.player.Playable;
import com.bx5a.minstrel.player.Player;
import com.bx5a.minstrel.utils.PlayableFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of Playable interface for the YouTube api
 */
public class YoutubeVideo implements Playable {
    public static void registerToFactory(PlayableFactory factory) {
        factory.register(new PlayableFactory.PlayableCreator() {
            @Override
            public Playable create(String id) throws PlayableCreationException {
                YoutubeVideo playable = new YoutubeVideo();
                try {
                    playable.initFromId(id);
                } catch (IOException e) {
                    throw new PlayableCreationException("Couldn't init YoutubeVideo: " + e.getMessage());
                }
                return playable;
            }
        }, getStaticClassName());
    }

    private static String getStaticClassName() {
        return "com.bx5a.minstrel.youtube.YoutubeVideo";
    }

    private String id;
    protected String title;
    private String thumbnailURL;
    private String highResolutionThumbnailURL;
    private BigInteger viewCount;
    private String duration;

    public static ArrayList<YoutubeVideo> search(String keywords) throws IOException {
        ArrayList<YoutubeVideo> videos = new ArrayList<>();
        YoutubeSearchEngine.SearchList<YoutubeVideo> videoList =
                YoutubeSearchEngine.getInstance().search(keywords);
        for (YoutubeVideo video : videoList.execute()) {
            videos.add(video);
        }
        return videos;
    }

    public void initFromId(String id) throws IOException {
        ArrayList<String> ids = new ArrayList<>();
        ids.add(id);
        YoutubeSearchEngine.SearchList<YoutubeVideo> videoList =
                YoutubeSearchEngine.getInstance().getVideoDetails(ids);
        List<YoutubeVideo> videos = videoList.execute();
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

    private void load() {
        YoutubePlayer.getInstance().load(this);
    }


    private boolean isLoaded() {
        String playingId = YoutubePlayer.getInstance().getLoadedId();
        if (playingId.isEmpty()) {
            return false;
        }
        return getId().equals(playingId);
    }

    @Override
    public String getClassName() {
        return getStaticClassName();
    }

    @Override
    public void play() {
        if (!isLoaded()) {
            load();
        }
        YoutubePlayer.getInstance().play();
    }

    @Override
    public void pause() {
        YoutubePlayer.getInstance().pause();
    }

    @Override
    public String getTitle() {
        return title;
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
    public String getHighResolutionThumbnailURL() {
        return highResolutionThumbnailURL;
    }

    @Override
    public String getDuration() {
        // youtube duration is ISO 8601 string (format is PT[MINUTES]M[SECONDS]S)
        Pattern pattern = Pattern.compile("PT(.*)M(.*)S");
        Matcher matcher = pattern.matcher(duration);
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

    public void setId(String id) {
        this.id = id;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setThumbnailURL(String thumbnailURL) {
        this.thumbnailURL = thumbnailURL;
    }

    public void setHighResolutionThumbnailURL(String url) {
        highResolutionThumbnailURL = url;
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
                YoutubeSearchEngine.SearchList<String> idList =
                        YoutubeSearchEngine.getInstance().relatedVideoIds(params[0]);
                List<String> relatedIds = idList.execute();
                ArrayList<Playable> result = new ArrayList<>();
                for (String id : relatedIds) {
                    YoutubeVideo related = new YoutubeVideo();
                    related.initFromId(id);
                    result.add(related);
                }
                return result;
            } catch (IOException e) {
                Log.w("YoutubeVideo", "Couldn't retrieve related: " + e.getMessage());
                e.printStackTrace();
                return new ArrayList<>();
            }
        }

        @Override
        protected void onPostExecute(List<Playable> relatedList) {
            super.onPostExecute(relatedList);
            eventListener.onRelatedAvailable(relatedList);
        }
    }

    @Override
    public boolean isEqual(Playable playable) {
        return playable.getId().equals(getId());
    }
}
