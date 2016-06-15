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

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import java.util.List;

import timber.log.Timber;

/**
 * Fragment for the search interface
 */
public class SearchFragment extends Fragment {
    private ListView resultList;
    private SearchView searchView;
    private InfiniteScrollWidget<YoutubeVideo> infiniteScrollWidget;
    private String keywords;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View view = layoutInflater.inflate(R.layout.fragment_search, null);

        InfiniteScrollWidget.AdapterCreator<YoutubeVideo> adapterCreator =
                new InfiniteScrollWidget.AdapterCreator<YoutubeVideo>() {
                    @Override
                    public ArrayAdapter<YoutubeVideo> create(Context context, List<YoutubeVideo> elementList) {
                        return new SearchItemAdapter(context, elementList);
                    }
                };

        keywords = null;

        infiniteScrollWidget = new InfiniteScrollWidget<YoutubeVideo>(view,
                R.id.viewSearch_resultList,
                R.id.viewSearch_progressBar,
                adapterCreator) {
            @Override
            protected SearchList<YoutubeVideo> getSearchList() {
                try {
                    return YoutubeSearchEngine.getInstance().search(keywords);
                } catch (IOException e) {
                    Timber.e(e, "Couldn't search youtube");
                    return null;
                }
            }
        };


        resultList = (ListView) view.findViewById(R.id.viewSearch_resultList);
        searchView = (SearchView) view.findViewById(R.id.viewSearch_search);

        connectEvents();

        // open search view
        searchView.setIconified(false);

        return view;
    }

    private void connectEvents() {
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
    }

    private void addPlayableToPlayer(Playable playable) {
        MasterPlayer.getInstance().enqueue(playable, Position.Next);
    }

    private void search(String keywords) {
        this.keywords = keywords;
        infiniteScrollWidget.displayFirstPage();
    }
}
