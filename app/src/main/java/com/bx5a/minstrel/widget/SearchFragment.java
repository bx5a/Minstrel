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

package com.bx5a.minstrel.widget;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

import com.bx5a.minstrel.R;
import com.bx5a.minstrel.player.MasterPlayer;
import com.bx5a.minstrel.player.Playable;
import com.bx5a.minstrel.player.Position;
import com.bx5a.minstrel.utils.IdleManager;
import com.bx5a.minstrel.youtube.YoutubeVideo;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Fragment for the search interface
 */
public class SearchFragment extends Fragment {
    private ListView resultList;
    private SearchView searchView;
    private String nextSearch;
    private ExecutorService searchQueue;
    private ArrayList<Future<?>> pendingSearch;


    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View view = layoutInflater.inflate(R.layout.fragment_search, null);

        resultList = (ListView) view.findViewById(R.id.viewSearch_resultList);
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
        resultList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Object o = view.getTag();
                if (o == null) {
                    return false;
                }
                Playable playable = (Playable) o;
                try {
                    PlayableDialogFragment fragment = new PlayableDialogFragment();
                    fragment.initForSearch(getContext(), playable, position);
                    fragment.show(getActivity().getSupportFragmentManager(), "Enqueue");
                } catch (IndexOutOfBoundsException exception) {
                    Log.w("HistoryFragment", "Can't get long pressed playable: " + exception.getMessage());
                }
                return true;
            }
        });


        // only one thread to make a serial queue
        searchQueue = Executors.newFixedThreadPool(1);

        // search view
        searchView = (SearchView) view.findViewById(R.id.viewSearch_search);
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
                // TODO: should be done on LowBrightnessOnIdleActivity but I can't figure out where
                // we get keyboard press notification
                IdleManager.getInstance().resetIdleTimer();
                asyncSearch(newText);
                return true;
            }
        });

        // open search view
        searchView.setIconified(false);

        return view;
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
        resultList.setAdapter(new SearchItemAdapter(getContext(), videos));
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
        private SearchFragment search;
        private String keywords;
        private ArrayList<YoutubeVideo> videos;

        public AsyncVideosSearch(SearchFragment search, String searchKeywords) {
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
            videos = YoutubeVideo.search(keywords);

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
