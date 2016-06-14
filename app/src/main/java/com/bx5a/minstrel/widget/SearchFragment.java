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

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.bx5a.minstrel.utils.SearchList;
import com.bx5a.minstrel.youtube.YoutubeSearchEngine;
import com.bx5a.minstrel.youtube.YoutubeVideo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for the search interface
 */
public class SearchFragment extends Fragment {
    private ListView resultList;
    private SearchView searchView;
    private AsyncTask searchTask;
    private SearchList<YoutubeVideo> searchList;
    private SearchItemAdapter adapter;
    private ArrayList<YoutubeVideo> videoList;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View view = layoutInflater.inflate(R.layout.fragment_search, null);

        videoList = new ArrayList<>();
        adapter = new SearchItemAdapter(getContext(), videoList);

        resultList = (ListView) view.findViewById(R.id.viewSearch_resultList);
        resultList.setAdapter(adapter);

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
                PlayableDialogFragment fragment = new PlayableDialogFragment();
                fragment.initForSearch(getContext(), playable, position);
                fragment.show(getActivity().getSupportFragmentManager(), "Enqueue");
                return true;
            }
        });

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
                IdleManager.getInstance().resetIdleTimer();

                // we get keyboard press notification
                search(newText);
                return true;
            }
        });

        // open search view
        searchView.setIconified(false);

        searchTask = null;
        return view;
    }

    private void addPlayableToPlayer(Playable playable) {
        MasterPlayer.getInstance().enqueue(playable, Position.Next);
    }

    private boolean isSearching() {
        return searchTask != null && searchTask.getStatus() != AsyncTask.Status.FINISHED;
    }

    private void search(String keywords) {
        if (isSearching()) {
            searchTask.cancel(true);
        }
        InitSearchTask initTask = new InitSearchTask(keywords);
        initTask.execute();
        searchTask = initTask;
    }

    private void clear() {
        videoList.clear();
        adapter.notifyDataSetChanged();
    }

    private void add(List<YoutubeVideo> videos) {
        videoList.addAll(videos);
        adapter.notifyDataSetChanged();
    }

    private void setSearchList(SearchList<YoutubeVideo> list) {
        searchList = list;
        updateList();
    }

    private void updateList() {
        if (isSearching()) {
            searchTask.cancel(true);
        }
        ExecuteSearchTask executeTask = new ExecuteSearchTask(searchList);
        executeTask.execute();
        searchTask = executeTask;
    }

    private class InitSearchTask extends AsyncTask<Void, Void, SearchList<YoutubeVideo>> {
        private boolean isCancelled;
        private String keywords;
        public InitSearchTask(String keywords) {
            isCancelled = false;
            this.keywords = keywords;
        }

        @Override
        protected void onPreExecute() {
            clear();
            // TODO: display waitbar
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(SearchList<YoutubeVideo> list) {
            // TODO: hide waitbar
            if (!isCancelled && list != null) {
                setSearchList(list);
            }
            super.onPostExecute(list);
        }

        @Override
        protected void onCancelled() {
            isCancelled = true;
            // TODO: hide waitbar
            super.onCancelled();
        }

        @Override
        protected SearchList<YoutubeVideo> doInBackground(Void... aVoid) {
            if (isCancelled) {
                return null;
            }

            try {
                return YoutubeSearchEngine.getInstance().search(keywords);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    private class ExecuteSearchTask extends AsyncTask<Void, Void, List<YoutubeVideo>> {
        private boolean isCancelled;
        private SearchList<YoutubeVideo> searchList;
        public ExecuteSearchTask(SearchList<YoutubeVideo> searchList) {
            isCancelled = false;
            this.searchList = searchList;
        }
        @Override
        protected void onPreExecute() {
            // TODO: display waitbar
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(List<YoutubeVideo> youtubeVideos) {
            if (!isCancelled && youtubeVideos != null) {
                add(youtubeVideos);
            }
            // TODO: hide waitbar
            super.onPostExecute(youtubeVideos);
        }

        @Override
        protected void onCancelled() {
            // TODO: hide waitbar
            super.onCancelled();
        }

        @Override
        protected List<YoutubeVideo> doInBackground(Void... aVoid) {
            if (isCancelled) {
                return null;
            }
            try {
                return searchList.getNextPage();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
