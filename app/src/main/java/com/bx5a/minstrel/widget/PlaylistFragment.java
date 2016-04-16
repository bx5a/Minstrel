package com.bx5a.minstrel.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.bx5a.minstrel.R;
import com.bx5a.minstrel.player.Playable;
import com.bx5a.minstrel.player.MasterPlayer;
import com.bx5a.minstrel.player.Playlist;

import java.util.ArrayList;

/**
 * Created by guillaume on 13/04/2016.
 */
public class PlaylistFragment extends Fragment {
    private ListView playlistView;

    private BroadcastReceiver playlistChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // on main thread
            Handler mainHandler = new Handler(context.getMainLooper());
            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    displayPlaylist();
                }
            };
            mainHandler.post(myRunnable);
        }
    };

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View view = layoutInflater.inflate(R.layout.fragment_playlist, null);
        playlistView = (ListView) view.findViewById(R.id.fragmentPlaylist_list);

        // connect the receivers
        IntentFilter currentSongIntentFilter = new IntentFilter("Minstrel.playlistChanged");
        getActivity().registerReceiver(playlistChangedReceiver, currentSongIntentFilter);

        return view;
    }

    private void displayPlaylist() {
        Playlist playlist = MasterPlayer.getInstance().getPlaylist();
        playlistView.setAdapter(new PlayableAdapter(getContext(), playlist));
    }
}
