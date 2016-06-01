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
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.bx5a.minstrel.R;
import com.bx5a.minstrel.player.MasterPlayerEventListener;
import com.bx5a.minstrel.player.MasterPlayer;
import com.bx5a.minstrel.player.Playable;
import com.bx5a.minstrel.player.Playlist;

/**
 * Fragment representing the current playlist
 */
public class PlaylistFragment extends Fragment {
    private ListView playlistView;
    private MasterPlayerEventListener eventListener;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View view = layoutInflater.inflate(R.layout.fragment_playlist, null);
        playlistView = (ListView) view.findViewById(R.id.fragmentPlaylist_list);

        initMasterPlayerEventListener();
        initItemEvents();

        displayPlaylist();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        MasterPlayer.getInstance().removeMasterPlayerEventListener(eventListener);
    }

    private void initMasterPlayerEventListener() {
        // event listener
        eventListener =  new MasterPlayerEventListener() {
            @Override
            public void onPlayStateChange() {

            }

            @Override
            public void onPlaylistChange() {
                displayPlaylist();
            }

            @Override
            public void onCurrentTimeChange() {

            }

            @Override
            public void onPlaylistFinish() {

            }
        };
        MasterPlayer.getInstance().addMasterPlayerEventListener(eventListener);
    }

    private void initItemEvents() {
        // on click on given item
        playlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    MasterPlayer.getInstance().setCurrentPlayableIndex((int) id);
                } catch (IndexOutOfBoundsException exception) {
                    // shouldn't happen
                    Log.e("PlaylistFragment", "Can't switch playable index: " + exception.getMessage());
                }
            }
        });
        playlistView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    Playable playable = MasterPlayer.getInstance().getPlaylist().get((int)id);
                    PlayableDialogFragment fragment = new PlayableDialogFragment();
                    fragment.initForPlaylist(getContext(), playable, position);
                    fragment.show(getActivity().getSupportFragmentManager(), "Enqueue");
                } catch (IndexOutOfBoundsException exception) {
                    Log.w("PlaylistFragment", "Can't get long pressed playable: " + exception.getMessage());
                }
                return true;
            }
        });
    }

    private void displayPlaylist() {
        Playlist playlist = MasterPlayer.getInstance().getPlaylist();
        playlistView.setAdapter(new PlaylistAdapter(getContext(), playlist));
    }
}
