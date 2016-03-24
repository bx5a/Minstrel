package com.bx5a.minstrel;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.ListView;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by guillaume on 23/03/2016.
 */
public class SearchActivity extends AppCompatActivity {

    private ListView resultList;
    private SearchView searchView;
    private AsyncSearchVideos asyncSearch;
    private String nextSearch;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // get the result list
        resultList = (ListView)findViewById(R.id.activitySearch_resultList);
        searchView = null;
        nextSearch = null;
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
        // if a search is running, do not start a new one. Just wait for it to finish and start the
        // next one after
        if (asyncSearch != null && asyncSearch.getStatus() == AsyncTask.Status.RUNNING) {
            nextSearch = keywords;
            return;
        }
        asyncSearch = new AsyncSearchVideos(this);
        asyncSearch.execute(keywords);
    }

    private void searchFinished(ArrayList<Video> videos) {
        // if we are asking for a next search, don't update the list. The next one will update it
        if (nextSearch != null) {
            asyncSearch(nextSearch);
            nextSearch = null;
            return;
        }
        updateList(videos);
    }

    private void updateList(ArrayList<Video> videos) {
        if (videos == null) {
            return;
        }
        resultList.setAdapter(new VideoAdapter(this, videos));
    }

    class AsyncSearchVideos extends AsyncTask<String, Integer, Boolean> {
        private SearchActivity context;
        private ArrayList<Video> videos;

        public AsyncSearchVideos(SearchActivity context) {
            super();
            this.context = context;
            this.videos = null;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            videos = Video.searchYoutube(context, params[0]);
            return videos != null;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            context.searchFinished(videos);
        }
    }
}
