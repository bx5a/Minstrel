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
import android.widget.GridView;
import android.widget.TextView;

import com.bx5a.minstrel.R;
import com.bx5a.minstrel.player.MasterPlayer;
import com.bx5a.minstrel.player.Playable;
import com.bx5a.minstrel.player.Position;
import com.bx5a.minstrel.utils.SearchList;

import java.util.List;

/**
 * Fragment representing A list of playable by showing thumbnail and title
 */
public abstract class ThumbnailPlayableListFragment extends Fragment {
    private TextView title;
    private GridView gridView;
    private InfiniteScrollWidget<Playable> infiniteScrollWidget;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View view = layoutInflater.inflate(R.layout.fragment_thumbnail_playable_list, null);

        InfiniteScrollWidget.AdapterCreator<Playable> adapterCreator =
                new InfiniteScrollWidget.AdapterCreator<Playable>() {
                    @Override
                    public ArrayAdapter<Playable> create(Context context, List<Playable> elementList) {
                        return new ThumbnailPlayableAdapter(context, elementList);
                    }
                };

        final ThumbnailPlayableListFragment currentObject = this;
        infiniteScrollWidget = new InfiniteScrollWidget<Playable>(view,
                R.id.fragmentThumbnailPlayableList_list,
                R.id.fragmentThumbnailPlayableList_progressBar,
                adapterCreator) {
            @Override
            protected SearchList<Playable> getSearchList() {
                return currentObject.getSearchList();
            }
        };


        title = (TextView) view.findViewById(R.id.fragmentThumbnailPlayableList_title);
        gridView = (GridView) view.findViewById(R.id.fragmentThumbnailPlayableList_list);
        connectEvents();
        infiniteScrollWidget.displayFirstPage();

        return view;
    }

    protected void setTitle(int resid) {
        title.setText(resid);
    }

    abstract protected SearchList<Playable> getSearchList();

    private void connectEvents() {
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MasterPlayer.getInstance().enqueue(
                        infiniteScrollWidget.getElementList().get(position), Position.Next);
            }
        });

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Playable playable = infiniteScrollWidget.getElementList().get(position);
                PlayableDialogFragment fragment = new PlayableDialogFragment();
                fragment.initForSearch(getContext(), playable, position);
                fragment.show(getActivity().getSupportFragmentManager(), "Enqueue");
                return true;
            }
        });
    }
}

