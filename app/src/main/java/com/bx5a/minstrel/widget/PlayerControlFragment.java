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
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bx5a.minstrel.R;
import com.bx5a.minstrel.player.MasterPlayer;
import com.bx5a.minstrel.player.MasterPlayerEventListener;
import com.bx5a.minstrel.player.Playable;
import com.bx5a.minstrel.player.Playlist;
import com.bx5a.minstrel.utils.ThumbnailManager;

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

        // TODO: understand why those 2 lines are necessary to match parent
        LinearLayout rootLayout = (LinearLayout) view.findViewById(R.id.fragmentPlayer_rootLayout);
        rootLayout.setLayoutParams(
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                              LinearLayout.LayoutParams.MATCH_PARENT));

        initSwipe(view.findViewById(R.id.fragmentPlayer_thumbnailLayout));
        connectPlayerEvents();
        connectButtonEvents();
        updateThumbnails();
        updatePlayPauseIcon();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        MasterPlayer.getInstance().removeMasterPlayerEventListener(playerEventListener);
    }

    private void initSwipe(View view) {
        final GestureDetector gestureDetector = new GestureDetector(getActivity(),
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                        try {
                            if (e1.getX() < e2.getX()) {
                                MasterPlayer.getInstance().previous();
                                return true;
                            }
                            MasterPlayer.getInstance().next();
                        } catch (IndexOutOfBoundsException exception) {
                            Log.i("PlayerControlFragment", "Couldn't change song: " + exception.getMessage());
                        } catch (IllegalStateException exception) {
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

    public void setOnPlaylistClickListener(View.OnClickListener listener) {
        onPlaylistClickListener = listener;
    }

    private void connectButtonEvents() {
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    MasterPlayer.getInstance().previous();
                } catch (IllegalStateException exception) {
                    Log.w("PlayerControlFragment", "Can't move to previous: " + exception.getMessage());
                } catch (IndexOutOfBoundsException exception) {
                    Log.i("PlayerControlFragment", exception.getMessage());
                }
            }
        });
        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (MasterPlayer.getInstance().isPlaying()) {
                        MasterPlayer.getInstance().pause();
                        return;
                    }
                    MasterPlayer.getInstance().play();
                } catch (IllegalStateException exception) {
                    Log.w("PlayerControlFragment", "Can't play: " + exception.getMessage());
                } catch (IndexOutOfBoundsException exception) {
                    Log.i("PlayerControlFragment", exception.getMessage());
                }
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    MasterPlayer.getInstance().next();
                } catch (IllegalStateException exception) {
                    Log.w("PlayerControlFragment", "Can't move to next: " + exception.getMessage());
                } catch (IndexOutOfBoundsException exception) {
                    Log.i("PlayerControlFragment", exception.getMessage());
                }
            }
        });
        playlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onPlaylistClickListener != null) {
                    onPlaylistClickListener.onClick(v);
                }
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

            }

            @Override
            public void onPlaylistFinish() {

            }
        };
        MasterPlayer.getInstance().addMasterPlayerEventListener(playerEventListener);
    }

    private void updatePlayPauseIcon() {
        if (MasterPlayer.getInstance().isPlaying()) {
            playPause.setBackgroundResource(R.drawable.ic_pausewhite);
            return;
        }
        playPause.setBackgroundResource(R.drawable.ic_playwhite);
    }

    private void updateThumbnails() {
        Playlist playlist = MasterPlayer.getInstance().getPlaylist();
        int currentIndex = MasterPlayer.getInstance().getCurrentPlayableIndex();

        updateThumbnail(playlist, currentIndex - 1, previousThumbnail, previousTitle);
        updateThumbnail(playlist, currentIndex, currentThumbnail, currentTitle);
        updateThumbnail(playlist, currentIndex + 1, nextThumbnail, nextTitle);
    }

    private void updateThumbnail(Playlist playlist, int playableIndex, final ImageView image, TextView text) {
        // reset to blank
        image.setImageResource(android.R.color.transparent);
        text.setText("");
        // update
        try {
            Playable playable = playlist.get(playableIndex);
            text.setText(playable.title());
            ThumbnailManager.getInstance().retreive(playable.getThumbnailURL(), new ThumbnailManager.BitmapAvailableListener() {
                @Override
                public void onBitmapAvailable(Bitmap bitmap) {
                    image.setImageBitmap(bitmap);
                }
            });
        } catch (IndexOutOfBoundsException exception) {
            Log.w("PlayerControlFragment", "Can't update thumbnail: " + exception.getMessage());
        }
    }
}
