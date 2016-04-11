package com.bx5a.minstrel;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.bx5a.minstrel.player.Player;
import com.bx5a.minstrel.player.Position;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by guillaume on 23/03/2016.
 */
public class SearchActivity extends AppCompatActivity {

    private ListView resultList;
    private SearchView searchView;
    private String nextSearch;
    private ExecutorService searchQueue;
    private ArrayList<Future<?>> pendingSearch;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // get the result list
        resultList = (ListView) findViewById(R.id.activitySearch_resultList);
        searchView = null;
        nextSearch = null;
        pendingSearch = new ArrayList<>();

        resultList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object o = view.getTag();
                if (o != null) {
                    Video video = (Video) o;
                    addVideoToPlayer(video);
                }
            }
        });

        // only one thread to make a serial queue
        searchQueue = Executors.newFixedThreadPool(1);
    }

    private void addVideoToPlayer(Video video) {
        Player.getInstance().enqueue(this, video, Position.Next);
        Player.getInstance().play(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu from XML
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.menuSearch_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);
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

        return super.onCreateOptionsMenu(menu);
    }

    private void asyncSearch(String keywords) {
        cancelPendingSearch();
        Future<?> search = searchQueue.submit(new AsyncVideosSearch(this, keywords));
        pendingSearch.add(search);
        // TODO: when destroying the SearchActivity we should shutdown() and awaitTermination()
    }

    private void searchFinished(ArrayList<Video> videos) {
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

    private void updateList(ArrayList<Video> videos) {
        if (videos == null) {
            return;
        }
        resultList.setAdapter(new VideoAdapter(this, videos));
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
        private SearchActivity context;
        private String keywords;
        private ArrayList<Video> videos;

        public AsyncVideosSearch(SearchActivity context, String searchKeywords) {
            super();
            this.context = context;
            this.keywords = searchKeywords;
            this.videos = null;
        }

        @Override
        public void run() {
            // search
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            videos = Video.searchYoutube(context, keywords);

            // notify on main thread
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            Handler mainHandler = new Handler(context.getMainLooper());
            Runnable runOnMainThread = new Runnable() {
                @Override
                public void run() {
                    context.searchFinished(videos);
                }
            };
            mainHandler.post(runOnMainThread);
        }
    }
}
