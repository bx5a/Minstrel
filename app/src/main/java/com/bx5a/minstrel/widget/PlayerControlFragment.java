package com.bx5a.minstrel.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bx5a.minstrel.R;
import com.bx5a.minstrel.player.MasterPlayer;
import com.bx5a.minstrel.player.Playlist;

/**
 * Created by guillaume on 11/04/2016.
 */
public class PlayerControlFragment extends Fragment {
    private TextView currentSongText;
    private TextView nextSongText;
    private ImageButton playPauseButton;
    private SeekBar seekBar;
    private GestureDetector gestureDetector;

    private BroadcastReceiver playlistChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // on main thread
            Handler mainHandler = new Handler(context.getMainLooper());
            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    displayCurrentAndNextSong();
                }
            };
            mainHandler.post(myRunnable);
        }
    };

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View view = layoutInflater.inflate(R.layout.fragment_player, null);

        currentSongText = (TextView) view.findViewById(R.id.viewPlayer_currentSong);
        nextSongText = (TextView) view.findViewById(R.id.viewPlayer_nextSong);
        playPauseButton = (ImageButton) view.findViewById(R.id.viewPlayer_playPause);
        seekBar = (SeekBar) view.findViewById(R.id.viewPlayer_seekBar);

        // connect the receivers
        IntentFilter currentSongIntentFilter = new IntentFilter("Minstrel.playlistChanged");
        getActivity().registerReceiver(playlistChangedReceiver, currentSongIntentFilter);

        // TODO: unregister receiver ?

        // button action
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MasterPlayer player = MasterPlayer.getInstance();
                if (player.isPlaying()) {
                    player.pause();
                    return;
                }
                player.play();
            }
        });

        // swipe detector
        gestureDetector = new GestureDetector(getActivity(),
                new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                try {
                    if (e1.getX() < e2.getX()) {
                        MasterPlayer.getInstance().next();
                        return true;
                    }
                    MasterPlayer.getInstance().previous();
                } catch (IndexOutOfBoundsException exception) {
                    Log.i("PlayerControlFragment", "Couldn't change song: " + exception.getMessage());
                }
                return true;
            }

            @Override
            public boolean onDown(MotionEvent e) {
                return true;

            }
        });

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });

        return view;
    }

    private void displayCurrentAndNextSong() {
        Playlist playlist = MasterPlayer.getInstance().getPlaylist();
        int currentSongIndex = MasterPlayer.getInstance().getCurrentPlayableIndex();

        if (currentSongIndex < playlist.size()) {
            currentSongText.setText(playlist.at(currentSongIndex).title());
        }
        if (currentSongIndex + 1 < playlist.size()) {
            nextSongText.setText(playlist.at(currentSongIndex + 1).title());
        }
    }
}
