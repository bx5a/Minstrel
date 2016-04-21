package com.bx5a.minstrel.widget;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.bx5a.minstrel.R;
import com.bx5a.minstrel.player.History;
import com.bx5a.minstrel.player.Playable;

import java.util.List;

/**
 * Created by guillaume on 21/04/2016.
 */
public class HistoryFragment extends Fragment {
    private GridView historyView;

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
        historyView.setAdapter(new HistoryAdapter(getContext(), playableList));
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

