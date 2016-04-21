package com.bx5a.minstrel.widget;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;

import com.bx5a.minstrel.R;
import com.bx5a.minstrel.player.MasterPlayer;
import com.bx5a.minstrel.player.Playable;
import com.bx5a.minstrel.player.Position;
import com.bx5a.minstrel.youtube.YoutubeVideo;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by guillaume on 23/03/2016.
 */
public class CustomSearchView extends LinearLayout {

    private ListView resultList;
    private SearchView searchView;
    private String nextSearch;
    private ExecutorService searchQueue;
    private ArrayList<Future<?>> pendingSearch;

    public CustomSearchView(Context context) {
        super(context);
        init(context);
    }

    public CustomSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomSearchView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_search, this);

        resultList = (ListView) findViewById(R.id.viewSearch_resultList);
        searchView = null;
        nextSearch = null;
        pendingSearch = new ArrayList<>();

        resultList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object o = view.getTag();
                if (o != null) {
                    Playable playable = (Playable) o;
                    addPlayableToPlayer(playable);
                }
            }
        });

        // only one thread to make a serial queue
        searchQueue = Executors.newFixedThreadPool(1);

        // search view
        searchView = (SearchView) findViewById(R.id.viewSearch_search);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (searchView != null) {
                    searchView.clearFocus();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                asyncSearch(newText);
                return true;
            }
        });
    }

    private void addPlayableToPlayer(Playable playable) {
        MasterPlayer.getInstance().enqueue(playable, Position.Next);
    }

    private void asyncSearch(String keywords) {
        cancelPendingSearch();
        Future<?> search = searchQueue.submit(new AsyncVideosSearch(this, keywords));
        pendingSearch.add(search);
        // TODO: when destroying should shutdown() and awaitTermination()
    }

    private void searchFinished(ArrayList<YoutubeVideo> videos) {
        // if we are asking for a next search, don't update the list. The next one will update it
        if (nextSearch != null) {
            asyncSearch(nextSearch);
            nextSearch = null;
            return;
        }
        updateList(videos);

        // remove finished search
        clearPendingSearch();
    }

    private void updateList(ArrayList<YoutubeVideo> videos) {
        if (videos == null) {
            return;
        }
        resultList.setAdapter(new YoutubeVideoAdapter(getContext(), videos));
    }

    private void cancelPendingSearch() {
        for (Future<?> search : pendingSearch) {
            search.cancel(true);
        }
        pendingSearch.clear();
    }

    private void clearPendingSearch() {
        // do a copy not to modify the array we iterate on
        ArrayList<Future<?>> pendingSearchCopy = pendingSearch;
        for (Future<?> search : pendingSearchCopy) {
            if (search.isDone() || search.isCancelled()) {
                pendingSearch.remove(search);
            }
        }
    }

    class AsyncVideosSearch implements Runnable {
        private CustomSearchView search;
        private String keywords;
        private ArrayList<YoutubeVideo> videos;

        public AsyncVideosSearch(CustomSearchView search, String searchKeywords) {
            super();
            this.search = search;
            this.keywords = searchKeywords;
            this.videos = null;
        }

        @Override
        public void run() {
            // search
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            videos = YoutubeVideo.search(search.getContext(), keywords);

            // notify on main thread
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            Handler mainHandler = new Handler(search.getContext().getMainLooper());
            Runnable runOnMainThread = new Runnable() {
                @Override
                public void run() {
                    search.searchFinished(videos);
                }
            };
            mainHandler.post(runOnMainThread);
        }
    }
}
