package com.bx5a.minstrel.widget;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bx5a.minstrel.R;
import com.bx5a.minstrel.player.MasterPlayer;
import com.bx5a.minstrel.player.MasterPlayerEventListener;
import com.bx5a.minstrel.player.Playable;
import com.bx5a.minstrel.player.Playlist;
import com.bx5a.minstrel.utils.DisplayImageTask;

import org.w3c.dom.Text;

/**
 * Created by guillaume on 22/04/2016.
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
    private MasterPlayerEventListener playerEventListener;

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

    private void connectButtonEvents() {
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MasterPlayer.getInstance().previous();
            }
        });
        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                MasterPlayer.getInstance().next();
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

    private void updateThumbnail(Playlist playlist, int playableIndex, ImageView image, TextView text) {
        // reset to blank
        image.setImageResource(android.R.color.transparent);
        text.setText("");
        // update
        try {
            Playable playable = playlist.at(playableIndex);
            text.setText(playable.title());
            new DisplayImageTask(playable.getThumbnailURL(), image).execute();
        } catch (IndexOutOfBoundsException exception) {
            Log.w("PlayerControlFragment", "Can't update thumbnail: " + exception.getMessage());
        }
    }
}
