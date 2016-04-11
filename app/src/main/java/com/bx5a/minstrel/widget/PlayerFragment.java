package com.bx5a.minstrel.widget;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.bx5a.minstrel.R;

/**
 * Created by guillaume on 11/04/2016.
 */
public class PlayerFragment extends Fragment {
    private TextView currentSongText;
    private TextView nextSongText;
    private Button playPauseButton;
    private Button nextButton;
    private VideoView videoView;
    private SeekBar seekBar;

    private BroadcastReceiver currentSongChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String title = intent.getStringExtra("Title");
            currentSongText.setText(title);
        }
    };
    private BroadcastReceiver nextSongChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String title = intent.getStringExtra("Title");
            nextSongText.setText(title);
        }
    };

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View view = layoutInflater.inflate(R.layout.fragment_player, null);

        currentSongText = (TextView) view.findViewById(R.id.viewPlayer_currentSong);
        nextSongText = (TextView) view.findViewById(R.id.viewPlayer_nextSong);
        playPauseButton = (Button) view.findViewById(R.id.viewPlayer_playPause);
        nextButton = (Button) view.findViewById(R.id.viewPlayer_next);
        videoView = (VideoView) view.findViewById(R.id.viewPlayer_video);
        seekBar = (SeekBar) view.findViewById(R.id.viewPlayer_seekBar);

        // connect the receivers
        IntentFilter currentSongIntentFilter = new IntentFilter("Minstrel.currentSongChanged");
        getActivity().registerReceiver(currentSongChangedReceiver, currentSongIntentFilter);
        IntentFilter nextSongIntentFilter = new IntentFilter("Minstrel.nextSongChanged");
        getActivity().registerReceiver(nextSongChangedReceiver, nextSongIntentFilter);

        return view;
    }
}
