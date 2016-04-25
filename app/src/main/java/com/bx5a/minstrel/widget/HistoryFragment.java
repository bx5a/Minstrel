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

import com.bx5a.minstrel.R;
import com.bx5a.minstrel.player.History;
import com.bx5a.minstrel.player.MasterPlayer;
import com.bx5a.minstrel.player.Playable;
import com.bx5a.minstrel.player.Position;

import java.util.List;

/**
 * Created by guillaume on 21/04/2016.
 */
public class HistoryFragment extends Fragment {
    private GridView historyView;
    private List<Playable> playableList;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View view = layoutInflater.inflate(R.layout.fragment_history, null);
        historyView = (GridView) view.findViewById(R.id.fragmentHistory_list);
        displayHistory();
        return view;
    }


    private void displayHistory() {
        AsyncUpdate update = new AsyncUpdate();
        update.execute(History.getInstance());
    }

    private void updateWithList(List<Playable> playableList) {
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
        protected List<Playable> doInBackground(History... params) {
            return params[0].get();
        }

        @Override
        protected void onPostExecute(List<Playable> playableList) {
            super.onPostExecute(playableList);
            updateWithList(playableList);
        }
    }
}

