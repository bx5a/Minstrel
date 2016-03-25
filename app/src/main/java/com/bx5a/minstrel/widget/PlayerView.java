package com.bx5a.minstrel.widget;

import android.content.Context;
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
    TextView currentSongText;
    TextView nextSongText;
    Button playPauseButton;
    Button nextButton;
    VideoView videoView;
    SeekBar seekBar;

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
    }
}
