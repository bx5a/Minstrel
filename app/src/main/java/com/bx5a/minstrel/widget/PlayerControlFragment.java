package com.bx5a.minstrel.widget;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
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
import com.bx5a.minstrel.player.CurrentTimeUpdaterService;
import com.bx5a.minstrel.player.MasterPlayer;
import com.bx5a.minstrel.player.MasterPlayerEventListener;
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
    private MasterPlayerEventListener eventListener;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View view = layoutInflater.inflate(R.layout.fragment_player, null);

        currentSongText = (TextView) view.findViewById(R.id.viewPlayer_currentSong);
        nextSongText = (TextView) view.findViewById(R.id.viewPlayer_nextSong);
        playPauseButton = (ImageButton) view.findViewById(R.id.viewPlayer_playPause);
        seekBar = (SeekBar) view.findViewById(R.id.viewPlayer_seekBar);

        // button action
        initPlayPause();
        // swipe detector
        initSongSwipe(view);
        // seekBar update
        initSeekBar();

        // init event listener
        eventListener = new MasterPlayerEventListener() {
            @Override
            public void onPlayStateChange() {
                updatePlayState();
            }

            @Override
            public void onPlaylistChange() {
                displayCurrentAndNextSong();
            }

            @Override
            public void onCurrentTimeChange() {
                updateSeekBar();
            }
        };
        MasterPlayer.getInstance().addMasterPlayerEventListener(eventListener);

        displayCurrentAndNextSong();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        MasterPlayer.getInstance().removeMasterPlayerEventListener(eventListener);
    }

    private void initSeekBar() {
        // TODO: should be done in xml but it's so much more work...
        seekBar.getProgressDrawable().setColorFilter(Color.BLACK, android.graphics.PorterDuff.Mode.MULTIPLY);
        seekBar.getThumb().mutate().setAlpha(0);
        // init current time updater service
        Intent intent = new Intent(getActivity(), CurrentTimeUpdaterService.class);
        getActivity().startService(intent);
        // on click
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                float progress = (float)(seekBar.getProgress()) / seekBar.getMax();
                MasterPlayer.getInstance().seekTo(progress);
            }
        });
    }

    private void updateSeekBar() {
        seekBar.setProgress((int) (MasterPlayer.getInstance().getCurrentPosition() * seekBar.getMax()));
    }

    private void initPlayPause() {
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
    }

    private void updatePlayState() {
        if (MasterPlayer.getInstance().isPlaying()) {
            playPauseButton.setImageResource(R.drawable.ic_pause);
            return;
        }
        playPauseButton.setImageResource(R.drawable.ic_play);
    }

    private void initSongSwipe(View view) {
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
    }

    private void displayCurrentAndNextSong() {
        Playlist playlist = MasterPlayer.getInstance().getPlaylist();
        int currentSongIndex = MasterPlayer.getInstance().getCurrentPlayableIndex();

        // default values
        currentSongText.setText("");
        nextSongText.setText("");

        if (currentSongIndex < playlist.size()) {
            currentSongText.setText(playlist.at(currentSongIndex).title());
        }
        if (currentSongIndex + 1 < playlist.size()) {
            nextSongText.setText(playlist.at(currentSongIndex + 1).title());
        }
    }
}
