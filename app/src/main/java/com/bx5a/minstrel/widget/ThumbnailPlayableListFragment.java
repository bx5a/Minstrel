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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bx5a.minstrel.R;
import com.bx5a.minstrel.exception.NotInitializedException;
import com.bx5a.minstrel.player.History;
import com.bx5a.minstrel.player.MasterPlayer;
import com.bx5a.minstrel.player.Playable;
import com.bx5a.minstrel.player.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment representing A list of playable by showing thumbnail and title
 */
public abstract class ThumbnailPlayableListFragment extends Fragment {
    protected interface PlayablePages {
        boolean hastNextPlayablePage();
        List<Playable> getNextPlayablePage();
    }

    private GridView gridView;
    private ArrayList<Playable> playableList;
    private ProgressBar waitBar;
    private TextView title;
    private PlayablePages pages;
    private AsyncInit initTask;
    private AsyncGetNextPage getNextPageTask;
    private ThumbnailPlayableAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View view = layoutInflater.inflate(R.layout.fragment_thumbnail_playable_list, null);
        gridView = (GridView) view.findViewById(R.id.fragmentThumbnailPlayableList_list);

        waitBar = (ProgressBar) view.findViewById(R.id.fragmentThumbnailPlayableList_progressBar);
        waitBar.setIndeterminate(true);
        waitBar.setVisibility(View.GONE);

        title = (TextView) view.findViewById(R.id.fragmentThumbnailPlayableList_title);

        initTask = null;
        getNextPageTask = null;
        playableList = new ArrayList<Playable>();
        adapter = new ThumbnailPlayableAdapter(getContext(), this.playableList);
        gridView.setAdapter(adapter);

        initList();
        connectEvents();

        return view;
    }

    protected void setTitle(int resid) {
        title.setText(resid);
    }

    abstract protected PlayablePages getPlayables();

    private void initList() {
        if (!History.getInstance().isInitialized()) {
            History.getInstance().setContext(getContext());
        }

        initTask = new AsyncInit();
        initTask.execute();
    }

    private void setPages(PlayablePages pages) {
        this.pages = pages;
    }

    private boolean isListInitialized() {
        if (initTask == null) {
            return false;
        }
        return initTask.getStatus() == AsyncTask.Status.FINISHED;
    }

    private void displayNextPage() {
        if (!isListInitialized()) {
            throw new NotInitializedException("Can't display page if list isn't initialized");
        }

        if (!pages.hastNextPlayablePage()) {
            return;
        }

        if (getNextPageTask != null && getNextPageTask.getStatus() != AsyncTask.Status.FINISHED) {
            return;
        }
        getNextPageTask = new AsyncGetNextPage();
        getNextPageTask.execute();
    }

    private void addToPlayableList(List<Playable> playableList) {
        this.playableList.addAll(playableList);
        adapter.notifyDataSetChanged();
    }

    private void connectEvents() {
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MasterPlayer.getInstance().enqueue(playableList.get(position), Position.Next);
            }
        });

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Playable playable = playableList.get(position);
                PlayableDialogFragment fragment = new PlayableDialogFragment();
                fragment.initForSearch(getContext(), playable, position);
                fragment.show(getActivity().getSupportFragmentManager(), "Enqueue");
                return true;
            }
        });

        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                // if every item are show, load more
                if (firstVisibleItem + visibleItemCount == totalItemCount && isListInitialized()) {
                    displayNextPage();
                }
            }
        });
    }

    class AsyncInit extends AsyncTask<Integer, Integer, PlayablePages> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            waitBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected PlayablePages doInBackground(Integer... params) {
            return getPlayables();
        }

        @Override
        protected void onPostExecute(PlayablePages playablePages) {
            super.onPostExecute(playablePages);
            waitBar.setVisibility(View.GONE);
            setPages(playablePages);
        }
    }

    class AsyncGetNextPage extends AsyncTask<Integer, Integer, List<Playable>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            waitBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<Playable> doInBackground(Integer... params) {
            return pages.getNextPlayablePage();
        }

        @Override
        protected void onPostExecute(List<Playable> playables) {
            super.onPostExecute(playables);
            waitBar.setVisibility(View.GONE);
            addToPlayableList(playables);
        }
    }
}

