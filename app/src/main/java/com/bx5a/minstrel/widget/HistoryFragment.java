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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;

import com.bx5a.minstrel.R;
import com.bx5a.minstrel.player.History;
import com.bx5a.minstrel.player.MasterPlayer;
import com.bx5a.minstrel.player.Playable;
import com.bx5a.minstrel.player.Position;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {
    private GridView historyView;
    private List<Playable> playableList;
    private ProgressBar waitBar;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View view = layoutInflater.inflate(R.layout.fragment_history, null);
        historyView = (GridView) view.findViewById(R.id.fragmentHistory_list);

        waitBar = (ProgressBar) view.findViewById(R.id.fragmentHistory_progressBar);
        waitBar.setIndeterminate(true);
        waitBar.setVisibility(View.GONE);

        displayHistory();
        return view;
    }

    private void displayHistory() {
        AsyncUpdate update = new AsyncUpdate();
        update.execute(History.getInstance());
    }

    private void updateWithList(List<Playable> playableList) {
        // don't update if not visible
        if (!isVisible()) {
            return;
        }
        this.playableList = playableList;
        historyView.setAdapter(new HistoryAdapter(getContext(), playableList));
        connectEvents();
    }

    private void connectEvents() {
        historyView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MasterPlayer.getInstance().enqueue(playableList.get(position), Position.Next);
            }
        });
        historyView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    Playable playable = playableList.get(position);
                    PlayableDialogFragment fragment = new PlayableDialogFragment();
                    fragment.initForSearch(getContext(), playable, position);
                    fragment.show(getActivity().getSupportFragmentManager(), "Enqueue");
                } catch (IndexOutOfBoundsException exception) {
                    Log.w("HistoryFragment", "Can't get long pressed playable: " + exception.getMessage());
                }
                return true;
            }
        });
    }

    class AsyncUpdate extends AsyncTask<History, Integer, List<Playable>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            waitBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<Playable> doInBackground(History... params) {
            try {
                return params[0].get();
            } catch (NullPointerException e) {
                Log.w("HistoryFragment", "Couldn't retrieve history: " + e.getMessage());
                return new ArrayList<Playable>();
            }
        }

        @Override
        protected void onPostExecute(List<Playable> playableList) {
            super.onPostExecute(playableList);
            waitBar.setVisibility(View.GONE);
            updateWithList(playableList);
        }
    }
}

