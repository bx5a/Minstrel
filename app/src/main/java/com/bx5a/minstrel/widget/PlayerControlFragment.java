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

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bx5a.minstrel.R;
import com.bx5a.minstrel.exception.NoThumbnailAvailableException;
import com.bx5a.minstrel.player.MasterPlayer;
import com.bx5a.minstrel.player.MasterPlayerEventListener;
import com.bx5a.minstrel.player.Playable;
import com.bx5a.minstrel.player.Playlist;
import com.bx5a.minstrel.utils.ThumbnailManager;

/**
 * Fragment representing advanced player controls
 */
public class PlayerControlFragment extends Fragment {
    private ImageView previousThumbnail;
    private ImageView currentThumbnail;
    private ImageView nextThumbnail;
    private TextView previousTitle;
    private TextView currentTitle;
    private TextView nextTitle;
    private ImageButton previous;
    private ImageButton playPause;
    private ImageButton next;
    private ImageButton playlist;
    private SeekBar seekBar;
    private MasterPlayerEventListener playerEventListener;
    private View.OnClickListener onPlaylistClickListener;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View view = layoutInflater.inflate(R.layout.fragment_player, null);

        previousThumbnail = (ImageView) view.findViewById(R.id.fragmentPlayer_previousThumbnail);
        currentThumbnail = (ImageView) view.findViewById(R.id.fragmentPlayer_currentThumbnail);
        nextThumbnail = (ImageView) view.findViewById(R.id.fragmentPlayer_nextThumbnail);
        previousTitle = (TextView) view.findViewById(R.id.fragmentPlayer_previousTitle);
        currentTitle = (TextView) view.findViewById(R.id.fragmentPlayer_currentTitle);
        nextTitle = (TextView) view.findViewById(R.id.fragmentPlayer_nextTitle);
        previous = (ImageButton) view.findViewById(R.id.fragmentPlayer_previous);
        playPause = (ImageButton) view.findViewById(R.id.fragmentPlayer_playPause);
        next = (ImageButton) view.findViewById(R.id.fragmentPlayer_next);
        playlist = (ImageButton) view.findViewById(R.id.fragmentPlayer_playlist);
        seekBar = (SeekBar) view.findViewById(R.id.fragmentPlayer_seekBar);

        // TODO: understand why those 2 lines are necessary to match parent
        LinearLayout rootLayout = (LinearLayout) view.findViewById(R.id.fragmentPlayer_rootLayout);
        rootLayout.setLayoutParams(
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                              LinearLayout.LayoutParams.MATCH_PARENT));

        initSwipe(view.findViewById(R.id.fragmentPlayer_thumbnailLayout));
        initSeekBarControls();
        connectPlayerEvents();
        connectButtonEvents();
        updateThumbnails();
        updatePlayPauseIcon();
        updateSeekBar();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        MasterPlayer.getInstance().removeMasterPlayerEventListener(playerEventListener);
    }

    private void initSeekBarControls() {
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
                MasterPlayer.getInstance().seekTo(progress);
            }
        });
    }

    private void updateSeekBar() {
        seekBar.setProgress((int) (MasterPlayer.getInstance().getCurrentPosition() * seekBar.getMax()));
    }

    private void initSwipe(View view) {
        final GestureDetector gestureDetector = new GestureDetector(getActivity(),
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                        // TODO: factorize with PlayerControlBarFragment
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
                });

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
    }

    public void setOnPlaylistClickListener(View.OnClickListener listener) {
        onPlaylistClickListener = listener;
    }

    private void connectButtonEvents() {
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!MasterPlayer.getInstance().canMoveToPrevious()) {
                    return;
                }

                MasterPlayer.getInstance().previous();
            }
        });
        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MasterPlayer.getInstance().getPlaylist().isEmpty()) {
                    return;
                }

                if (MasterPlayer.getInstance().isPlaying()) {
                    MasterPlayer.getInstance().pause();
                    return;
                }
                MasterPlayer.getInstance().play();
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!MasterPlayer.getInstance().canMoveToNext()) {
                    return;
                }

                MasterPlayer.getInstance().next();
            }
        });
        playlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onPlaylistClickListener == null) {
                    return;
                }

                onPlaylistClickListener.onClick(v);
            }
        });
    }

    private void connectPlayerEvents() {
        playerEventListener = new MasterPlayerEventListener() {
            @Override
            public void onPlayStateChange() {
                updatePlayPauseIcon();
            }

            @Override
            public void onPlaylistChange() {
                updateThumbnails();
            }

            @Override
            public void onCurrentTimeChange() {
                updateSeekBar();
            }

            @Override
            public void onPlaylistFinish() {

            }
        };
        MasterPlayer.getInstance().addMasterPlayerEventListener(playerEventListener);
    }

    private void updatePlayPauseIcon() {
        if (MasterPlayer.getInstance().isPlaying()) {
            playPause.setBackgroundResource(getDefaultPauseIcon());
            return;
        }
        playPause.setBackgroundResource(getDefaultPlayIcon());
    }

    private int getDefaultPlayIcon() {
        return getDefaultIcon(R.attr.playIcon);
    }

    private int getDefaultPauseIcon() {
        return getDefaultIcon(R.attr.pauseIcon);
    }

    private int getDefaultIcon(int resid) {
        TypedValue value = new TypedValue();
        getContext().getTheme().resolveAttribute(resid, value, true);
        return value.resourceId;
    }

    private void updateThumbnails() {
        Playlist playlist = MasterPlayer.getInstance().getPlaylist();
        if (playlist.isEmpty()) {
            return;
        }
        int currentIndex = MasterPlayer.getInstance().getCurrentPlayableIndex();

        unsetThumbnail(previousThumbnail, previousTitle);
        unsetThumbnail(currentThumbnail, currentTitle);
        unsetThumbnail(nextThumbnail, nextTitle);

        if (playlist.hasIndex(currentIndex - 1)) {
            updateThumbnail(playlist, currentIndex - 1, previousThumbnail, previousTitle);
        }
        if (playlist.hasIndex(currentIndex)) {
            updateThumbnail(playlist, currentIndex, currentThumbnail, currentTitle);
        }
        if (playlist.hasIndex(currentIndex + 1)) {
            updateThumbnail(playlist, currentIndex + 1, nextThumbnail, nextTitle);
        }
    }

    private void unsetThumbnail(ImageView image, TextView text) {
        image.setImageResource(android.R.color.transparent);
        text.setText("");
    }

    private void updateThumbnail(Playlist playlist,
                                 int playableIndex,
                                 final ImageView image,
                                 TextView text) {
        Playable playable = playlist.get(playableIndex);
        text.setText(playable.getTitle());

        // try high resolution
        try {
            ThumbnailManager.getInstance().retreive(playable.getHighResolutionThumbnailURL(),
                    new ThumbnailManager.BitmapAvailableListener() {
                @Override
                public void onBitmapAvailable(Bitmap bitmap) {
                    image.setImageBitmap(bitmap);
                }
            });
            return;
        } catch (NoThumbnailAvailableException e) {
            Log.i("PlayerControlFragment", "Can't get high resolution thumbnail for playable " +
                    playable.getTitle() + " defaulting to low resolution");
        }

        // if fails, default to low resolution
        try {
            ThumbnailManager.getInstance().retreive(playable.getThumbnailURL(),
                    new ThumbnailManager.BitmapAvailableListener() {
                        @Override
                        public void onBitmapAvailable(Bitmap bitmap) {
                            image.setImageBitmap(bitmap);
                        }
                    });
        } catch (NoThumbnailAvailableException e1) {
            Log.i("PlayerControlFragment", "Can't get low resolution thumbnail for playable " +
                    playable.getTitle());
            e1.printStackTrace();
        }
    }
}
