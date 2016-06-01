package com.bx5a.minstrel;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import com.bx5a.minstrel.legacy.SoftKeyboardHandledLayout;
import com.bx5a.minstrel.player.History;
import com.bx5a.minstrel.player.MasterPlayer;
import com.bx5a.minstrel.player.Playable;
import com.bx5a.minstrel.player.PlaylistManager;
import com.bx5a.minstrel.player.Position;
import com.bx5a.minstrel.utils.LowBrightnessOnIdleActivity;
import com.bx5a.minstrel.widget.AboutFragment;
import com.bx5a.minstrel.widget.HistoryFragment;
import com.bx5a.minstrel.widget.ImageAndTextButton;
import com.bx5a.minstrel.widget.PlayerControlBarFragment;
import com.bx5a.minstrel.widget.PlayerControlFragment;
import com.bx5a.minstrel.widget.PlaylistFragment;
import com.bx5a.minstrel.widget.SearchFragment;
import com.bx5a.minstrel.widget.UndoDialogFragment;
import com.bx5a.minstrel.youtube.YoutubePlayer;
import com.bx5a.minstrel.youtube.YoutubeSearchEngine;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

public class MainActivity extends LowBrightnessOnIdleActivity {

    private SoftKeyboardHandledLayout drawerLayout;
    private ImageAndTextButton searchButton;
    private ImageAndTextButton playlistButton;
    private ImageAndTextButton historyButton;
    private ImageAndTextButton aboutButton;
    private PlayerControlBarFragment playerControls;
    private UndoDialogFragment undoDialogFragment;
    private final int kAutoDismissMilliseconds = 2000;

    // fragments
    private PlaylistFragment playlistFragment;
    private SearchFragment searchFragment;
    private HistoryFragment historyFragment;
    private PlayerControlFragment playerControlFragment;
    private AboutFragment aboutFragment;

    private final String kWasPlayingKey = "wasPlaying";
    private final String kPositionAtDestroyKey = "seekPosition";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = (SoftKeyboardHandledLayout) findViewById(R.id.activityMain_drawer);
        searchButton = (ImageAndTextButton) findViewById(R.id.activityMain_searchButton);
        playlistButton = (ImageAndTextButton) findViewById(R.id.activityMain_playlistButton);
        historyButton = (ImageAndTextButton) findViewById(R.id.activityMain_historyButton);
        aboutButton = (ImageAndTextButton) findViewById(R.id.activityMain_aboutButton);
        playerControls = (PlayerControlBarFragment) getSupportFragmentManager().findFragmentById(R.id.activityMain_playerControls);

        initBackground();
        initHistory();
        initYoutubePlayer();
        initMenuActions();
        initPlayerControl();
        initUndoDialog();
        initFragments();

        displayHistory();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        YoutubePlayer.getInstance().reset();
        History.getInstance().setContext(null);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(kWasPlayingKey, MasterPlayer.getInstance().isPlaying());
        outState.putFloat(kPositionAtDestroyKey, MasterPlayer.getInstance().getCurrentPosition());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (!savedInstanceState.getBoolean(kWasPlayingKey)) {
            return;
        }
        MasterPlayer.getInstance().playAt(savedInstanceState.getFloat(kPositionAtDestroyKey));
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
    public void onPause() {
        super.onPause();
        MasterPlayer.getInstance().unload();
    }

    @Override
    public void onBackPressed() {
        // if we still have some back stack, just do the regular
        if (getSupportFragmentManager().getBackStackEntryCount() != 0) {
            super.onBackPressed();
            return;
        }

        // if back stack is empty, pressing back will close the app. make sure that's what use wants
        new AlertDialog.Builder(this)
                .setTitle("Warning")
                .setMessage("Closing the app will stop playback. Do you really want to exit ?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        MainActivity.super.onBackPressed();
                    }
                }).create().show();
    }

    private void initBackground() {
        Bitmap logo = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        Point screenSize = new Point();
        getWindowManager().getDefaultDisplay().getSize(screenSize);
        Bitmap scaledLogo = Bitmap.createScaledBitmap(logo, screenSize.x, screenSize.x, true);
        ImageView backgroundLogo = (ImageView) findViewById(R.id.activityMain_backgroundLogo);
        backgroundLogo.setImageBitmap(scaledLogo);
    }

    private void openSidePanel() {
        drawerLayout.openDrawer(Gravity.START);
    }
    private void closeSidePanel() {
        drawerLayout.closeDrawer(Gravity.START);
    }

    private void initFragments() {
        historyFragment = new HistoryFragment();
        searchFragment = new SearchFragment();
        playlistFragment = new PlaylistFragment();
        playerControlFragment = new PlayerControlFragment();

        playerControlFragment.setOnPlaylistClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayPlaylist();
            }
        });
        aboutFragment = new AboutFragment();
    }

    private void initHistory() {
        History.getInstance().setContext(this);
    }

    private void initYoutubePlayer() {
        if (!YoutubeSearchEngine.getInstance().isInitialized()) {
            YoutubeSearchEngine.getInstance().init(this);
        }
        YoutubePlayer player = YoutubePlayer.getInstance();
        player.setPlayerFragment((YouTubePlayerSupportFragment) getSupportFragmentManager().findFragmentById(R.id.youtube_fragment));
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
        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayHistory();
            }
        });
        aboutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayAbout();
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
                displayPlayerControls();
            }
        });
    }

    private void initUndoDialog() {
        undoDialogFragment = new UndoDialogFragment();
        MasterPlayer.getInstance().setPlaylistManagerEventListener(new PlaylistManager.EventListener() {
            @Override
            public void onEnqueued(final int index, final int selectedIndex) {
                undoDialogFragment.setText("Added to list");
                undoDialogFragment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MasterPlayer.getInstance().remove(index);
                        if (MasterPlayer.getInstance().getCurrentPlayableIndex() != selectedIndex) {
                            MasterPlayer.getInstance().setCurrentPlayableIndex(selectedIndex);
                        }
                        undoDialogFragment.dismiss();
                    }
                });
                undoDialogFragment.show(getSupportFragmentManager(), "Undo");

                // dismiss if clicked outside
                Dialog dialog = undoDialogFragment.getDialog();
                if (dialog != null) {
                    dialog.setCanceledOnTouchOutside(true);
                }

                // auto dismiss
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

    // from http://stackoverflow.com/questions/1109022/close-hide-the-android-soft-keyboard
    private void closeKeyboard() {
        View view = getCurrentFocus();
        if (view == null) {
            Log.w("MainActivity", "Can't close keyboard: current focus view not found");
            return;
        }
        InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(view.getWindowToken(), 0);

        // show player controls
        getSupportFragmentManager().beginTransaction().show(playerControls).commit();
    }

    private void displayFragment(Fragment fragment) {
        closeKeyboard();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.activityMain_topPlaceholder, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void displayPlaylist() {
        displayFragment(playlistFragment);
        closeSidePanel();
    }

    private void displaySearch() {
        displayFragment(searchFragment);
        closeSidePanel();
    }

    private void displayHistory() {
        displayFragment(historyFragment);
        closeSidePanel();
    }

    private void displayPlayerControls() {
        displayFragment(playerControlFragment);
        closeSidePanel();
    }

    private void displayAbout() {
        displayFragment(aboutFragment);
        closeSidePanel();
    }
}
