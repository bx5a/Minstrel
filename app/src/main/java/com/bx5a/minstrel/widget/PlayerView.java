package com.bx5a.minstrel.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.bx5a.minstrel.R;

/**
 * Created by guillaume on 24/03/2016.
 */
public class PlayerView extends LinearLayout {
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

    public PlayerView(Context context) {
        super(context);
    }

    public PlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_player, this, true);

        currentSongText = (TextView)findViewById(R.id.viewPlayer_currentSong);
        nextSongText = (TextView)findViewById(R.id.viewPlayer_nextSong);
        playPauseButton = (Button)findViewById(R.id.viewPlayer_playPause);
        nextButton = (Button)findViewById(R.id.viewPlayer_next);
        videoView = (VideoView)findViewById(R.id.viewPlayer_video);
        seekBar = (SeekBar)findViewById(R.id.viewPlayer_seekBar);

        // connect the receivers
        IntentFilter currentSongIntentFilter = new IntentFilter("Minstrel.currentSongChanged");
        context.registerReceiver(currentSongChangedReceiver, currentSongIntentFilter);
        IntentFilter nextSongIntentFilter = new IntentFilter("Minstrel.nextSongChanged");
        context.registerReceiver(nextSongChangedReceiver, nextSongIntentFilter);
    }
}
