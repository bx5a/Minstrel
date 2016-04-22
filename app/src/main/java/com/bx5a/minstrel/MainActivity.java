package com.bx5a.minstrel;

import android.content.DialogInterface;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.bx5a.minstrel.legacy.SoftKeyboardHandledLayout;
import com.bx5a.minstrel.player.History;
import com.bx5a.minstrel.player.MasterPlayer;
import com.bx5a.minstrel.player.Playable;
import com.bx5a.minstrel.player.Position;
import com.bx5a.minstrel.widget.HistoryFragment;
import com.bx5a.minstrel.widget.ImageAndTextButton;
import com.bx5a.minstrel.widget.PlayerControlFragment;
import com.bx5a.minstrel.widget.PlaylistFragment;
import com.bx5a.minstrel.widget.SearchFragment;
import com.bx5a.minstrel.widget.UndoDialogFragment;
import com.bx5a.minstrel.youtube.DeveloperKey;
import com.bx5a.minstrel.youtube.YoutubePlayer;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

public class MainActivity extends AppCompatActivity {

    private SoftKeyboardHandledLayout drawerLayout;
    private ImageAndTextButton searchButton;
    private ImageAndTextButton playlistButton;
    private PlayerControlFragment playerControls;
    private UndoDialogFragment undoDialogFragment;
    private final int kAutoDismissMilliseconds = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = (SoftKeyboardHandledLayout) findViewById(R.id.activityMain_drawer);
        searchButton = (ImageAndTextButton) findViewById(R.id.activityMain_searchButton);
        playlistButton = (ImageAndTextButton) findViewById(R.id.activityMain_playlistButton);
        playerControls = (PlayerControlFragment) getSupportFragmentManager().findFragmentById(R.id.activityMain_playerControls);

        initHistory();
        initYoutubePlayer();
        initMenuActions();
        initPlayerControl();
        initUndoDialog();

        displayHistory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        YoutubePlayer.getInstance().reset();
        History.getInstance().setContext(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuMain_search:
                openSidePanel();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void openSidePanel() {
        drawerLayout.openDrawer(Gravity.START);
    }
    private void closeSidePanel() {
        drawerLayout.closeDrawer(Gravity.START);
    }

    private void initHistory() {
        History.getInstance().setContext(this);
    }

    private void initYoutubePlayer() {
        YouTubePlayerSupportFragment fragment = (YouTubePlayerSupportFragment) getSupportFragmentManager().findFragmentById(R.id.youtube_fragment);
        fragment.initialize(DeveloperKey.DEVELOPER_KEY, new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                YoutubePlayer.getInstance().setYoutubePlayer(youTubePlayer);
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                Log.e("Youtube", youTubeInitializationResult.toString());
            }
        });
    }

    private void initMenuActions() {
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displaySearch();
            }
        });
        playlistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayPlaylist();
            }
        });
    }

    private void initPlayerControl() {
        // keyboard show / hide
        drawerLayout.setOnSoftKeyboardVisibilityChangeListener(new SoftKeyboardHandledLayout.SoftKeyboardVisibilityChangeListener() {
            @Override
            public void onSoftKeyboardShow() {
                getSupportFragmentManager().beginTransaction().hide(playerControls).commit();
            }

            @Override
            public void onSoftKeyboardHide() {
                getSupportFragmentManager().beginTransaction().show(playerControls).commit();
            }
        });

        // player control pressed
        playerControls.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayPlaylist();
            }
        });
    }

    private void initUndoDialog() {
        undoDialogFragment = new UndoDialogFragment();
        MasterPlayer.getInstance().setOnEnqueuedListener(new MasterPlayer.OnPlayableEnqueuedListener() {
            @Override
            public void onEnqueued(final Playable playable, final Position position) {
                undoDialogFragment.setText(playable.title() + " enqueued.");
                undoDialogFragment.setOnClickListener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MasterPlayer.getInstance().dequeue(playable, position);
                    }
                });
                undoDialogFragment.show(getSupportFragmentManager(), "Undo");

                // auto dismiss in 1 second
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        undoDialogFragment.dismiss();
                    }
                }, kAutoDismissMilliseconds);
            }
        });
    }

    private void displayFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.activityMain_topPlaceholder, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void displayPlaylist() {
        PlaylistFragment fragment = new PlaylistFragment();
        displayFragment(fragment);
        closeSidePanel();
    }

    private void displaySearch() {
        SearchFragment fragment = new SearchFragment();
        displayFragment(fragment);
        closeSidePanel();
    }

    private void displayHistory() {
        HistoryFragment fragment = new HistoryFragment();
        displayFragment(fragment);
        closeSidePanel();
    }
}
