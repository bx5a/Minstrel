package com.bx5a.minstrel.youtube;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.bx5a.minstrel.LocalSQLiteOpenHelper;
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
        // TODO: should be a singleton ?
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

    /*public static List<YoutubeVideo> history(Context context) {
        YoutubeSearchEngine searchEngine = new YoutubeSearchEngine(context);
        LocalSQLiteOpenHelper helper = new LocalSQLiteOpenHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor cursor = db.query(true, "PlayedYoutubeVideo", new String[]{"id", "videoId"},
                null, null, null, null, "id", null);

        ArrayList<String> videoIds = new ArrayList<>();
        while(cursor.moveToNext()) {
            // create video from videoId
            videoIds.add(cursor.getString(cursor.getColumnIndex("videoId")));
        }
        cursor.close();
        db.close();

        try {
            return searchEngine.getVideoDetails(videoIds);
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }*/

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
