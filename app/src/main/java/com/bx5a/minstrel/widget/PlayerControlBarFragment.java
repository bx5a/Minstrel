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

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bx5a.minstrel.R;
import com.bx5a.minstrel.exception.NotInitializedException;
import com.bx5a.minstrel.player.CurrentTimeUpdaterService;
import com.bx5a.minstrel.player.MasterPlayer;
import com.bx5a.minstrel.player.MasterPlayerEventListener;
import com.bx5a.minstrel.player.Playlist;

import timber.log.Timber;

public class PlayerControlBarFragment extends Fragment {
    private TextView currentSongText;
    private TextView nextSongText;
    private ImageButton playPauseButton;
    private SeekBar seekBar;
    private GestureDetector gestureDetector;
    private MasterPlayerEventListener eventListener;
    private OnClickListener onClickListener;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View view = layoutInflater.inflate(R.layout.fragment_player_bar, null);

        currentSongText = (TextView) view.findViewById(R.id.viewPlayer_currentSong);
        nextSongText = (TextView) view.findViewById(R.id.viewPlayer_nextSong);
        playPauseButton = (ImageButton) view.findViewById(R.id.viewPlayer_playPause);
        seekBar = (SeekBar) view.findViewById(R.id.viewPlayer_seekBar);
        onClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        };

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

            @Override
            public void onPlaylistFinish() {

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

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    private void initSeekBar() {
        // TODO: should be done in xml but it's so much more work...
        seekBar.getProgressDrawable().setColorFilter(Color.BLACK, android.graphics.PorterDuff.Mode.MULTIPLY);
        //seekBar.getThumb().mutate().setAlpha(0);
        seekBar.setPadding(0, 0, 0, 0);
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
                // can seek on an empty playlist
                if (MasterPlayer.getInstance().getPlaylist().isEmpty()) {
                    return;
                }
                float progress = (float) (seekBar.getProgress()) / seekBar.getMax();
                try {
                    MasterPlayer.getInstance().seekTo(progress);
                } catch (NotInitializedException e) {
                    Timber.e(e, "Can't seek because playable's player isn't initialized");
                }
            }
        });
    }

    private void updateSeekBar() {
        seekBar.setProgress((int) (MasterPlayer.getInstance().getCurrentPosition() * seekBar.getMax()));
    }

    private void initPlayPause() {
        playPauseButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MasterPlayer player = MasterPlayer.getInstance();
                // can't start an empty playlist
                if (player.getPlaylist().isEmpty()) {
                    return;
                }

                if (player.isPlaying()) {
                    // if is playing, we take for granted that this playable's player is initialized
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

    private void initSongSwipe(final View view) {
        gestureDetector = new GestureDetector(getActivity(),
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                        MasterPlayer player = MasterPlayer.getInstance();
                        boolean moveToNextRequested = e1.getX() > e2.getX();

                        // TODO: display no next/previous available messages ??
                        if (moveToNextRequested && !player.canMoveToNext()) {
                            return true;
                        }
                        if (!moveToNextRequested && !player.canMoveToPrevious()) {
                            return true;
                        }

                        // do previous / next
                        if (moveToNextRequested) {
                            player.next();
                        } else {
                            player.previous();
                        }
                        return true;
                    }

                    @Override
                    public boolean onDown(MotionEvent e) {
                        return true;

                    }

                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent e) {
                        onClickListener.onClick(view);
                        return super.onSingleTapConfirmed(e);
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
        currentSongText.setText("");
        nextSongText.setText("");

        Playlist playlist = MasterPlayer.getInstance().getPlaylist();
        if (playlist.isEmpty()) {
            return;
        }

        int currentSongIndex = MasterPlayer.getInstance().getCurrentPlayableIndex();
        if (playlist.hasIndex(currentSongIndex)) {
            currentSongText.setText(playlist.get(currentSongIndex).getTitle());
        }
        if (playlist.hasIndex(currentSongIndex + 1)) {
            nextSongText.setText(playlist.get(currentSongIndex + 1).getTitle());
        }
    }
}
