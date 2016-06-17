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

package com.bx5a.minstrel;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bx5a.minstrel.legacy.SoftKeyboardHandledLayout;
import com.bx5a.minstrel.player.History;
import com.bx5a.minstrel.player.MasterPlayer;
import com.bx5a.minstrel.player.MasterPlayerEventListener;
import com.bx5a.minstrel.player.Playable;
import com.bx5a.minstrel.player.Playlist;
import com.bx5a.minstrel.player.PlaylistManager;
import com.bx5a.minstrel.player.Position;
import com.bx5a.minstrel.utils.GeneralPreferencesEnum;
import com.bx5a.minstrel.utils.LowBrightnessOnIdleActivity;
import com.bx5a.minstrel.utils.MotionEventHandler;
import com.bx5a.minstrel.widget.AboutFragment;
import com.bx5a.minstrel.widget.GeneralPreferenceFragment;
import com.bx5a.minstrel.widget.HistoryFragment;
import com.bx5a.minstrel.widget.ImageAndTextButton;
import com.bx5a.minstrel.widget.PlayerControlBarFragment;
import com.bx5a.minstrel.widget.PlayerControlFragment;
import com.bx5a.minstrel.widget.PlaylistFragment;
import com.bx5a.minstrel.widget.PopularFragment;
import com.bx5a.minstrel.widget.SearchFragment;
import com.bx5a.minstrel.widget.UndoDialogFragment;
import com.bx5a.minstrel.youtube.YoutubePlayer;
import com.bx5a.minstrel.youtube.YoutubeSearchEngine;
import com.crashlytics.android.Crashlytics;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

import static android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences;

/**
 * Only activity of the application. Content of every fragments
 */
public class MainActivity extends LowBrightnessOnIdleActivity {

    private SoftKeyboardHandledLayout drawerLayout;
    private ImageAndTextButton searchButton;
    private ImageAndTextButton playlistButton;
    private ImageAndTextButton historyButton;
    private ImageAndTextButton aboutButton;
    private ImageAndTextButton preferenceButton;
    private PlayerControlBarFragment playerControls;
    private UndoDialogFragment undoDialogFragment;
    private RelativeLayout playerControlsParentLayout;
    private LinearLayout youtubeFragmentHolder;
    private final int AUTO_DISMISS_MILLISECOND = 2000;

    private boolean currentThemeIsDark;

    // fragments
    private PlaylistFragment playlistFragment;
    private SearchFragment searchFragment;
    private HistoryFragment historyFragment;
    private PopularFragment popularFragment;
    private PlayerControlFragment playerControlFragment;
    private AboutFragment aboutFragment;
    private GeneralPreferenceFragment generalPreferenceFragment;

    private final String kWasPlayingKey = "wasPlaying";
    private final String kPositionAtDestroyKey = "seekPosition";

    private SharedPreferences.OnSharedPreferenceChangeListener preferencesListener;
    private MasterPlayerEventListener playerEventListener;
    private MotionEventHandler motionEventHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        initTheme();
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        // init logging options
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        updateScreenRotationPreference();

        setContentView(R.layout.activity_main);

        drawerLayout = (SoftKeyboardHandledLayout) findViewById(R.id.activityMain_drawer);
        searchButton = (ImageAndTextButton) findViewById(R.id.activityMain_searchButton);
        playlistButton = (ImageAndTextButton) findViewById(R.id.activityMain_playlistButton);
        historyButton = (ImageAndTextButton) findViewById(R.id.activityMain_historyButton);
        aboutButton = (ImageAndTextButton) findViewById(R.id.activityMain_aboutButton);
        preferenceButton = (ImageAndTextButton) findViewById(R.id.activityMain_preferenceButton);
        playerControlsParentLayout = (RelativeLayout) findViewById(R.id.activityMain_bottomControls);
        youtubeFragmentHolder = (LinearLayout) findViewById(R.id.activityMain_youtubeFragmentHolder);
        playerControls = (PlayerControlBarFragment) getSupportFragmentManager().findFragmentById(R.id.activityMain_playerControls);

        initBackground();
        initYoutubePlayer();
        initMenuActions();
        initPlayerControl();
        initFragments();
        initPreferencesListener();
        initTitleClickEvent();
        initMotionEventHandler();

        displayInitialFragment();
        updateControlBarVisibility();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        motionEventHandler.dispatchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }



    @Override
    protected void onDestroy() {
        deinitPreferencesListener();
        super.onDestroy();
        YoutubePlayer.getInstance().reset();
    }

    @Override
    protected void onResume() {
        initMasterPlayerBehavior();
        initOnEnqueueEvent();
        super.onResume();
    }

    @Override
    public void onPause() {
        deInitMasterPlayerBehavior();
        deInitOnEnqueueEvent();
        super.onPause();
        MasterPlayer.getInstance().unload();
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
        popularFragment = new PopularFragment();

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
        generalPreferenceFragment = new GeneralPreferenceFragment();
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
        preferenceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayPreference();
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

    private void initOnEnqueueEvent() {
        undoDialogFragment = new UndoDialogFragment();
        MasterPlayer.getInstance().setPlaylistManagerEventListener(new PlaylistManager.EventListener() {
            @Override
            public void onEnqueued(int index, int selectedIndex) {
                displayUndoDialog(index, selectedIndex);

                // Add cued song to history
                // if we don't have enough playable in list, that's an error
                if (MasterPlayer.getInstance().getPlaylist().size() <= index) {
                    return;
                }
                Playable playable = MasterPlayer.getInstance().getPlaylist().get(index);
                History history = History.getInstance();
                history.store(playable);
            }
        });
    }

    private void deInitOnEnqueueEvent() {
        MasterPlayer.getInstance().setPlaylistManagerEventListener(new PlaylistManager.EventListener() {
            @Override
            public void onEnqueued(int enqueuedIndex, int selectedIndex) {

            }
        });
    }

    private void displayUndoDialog(final int index, final int selectedIndex) {
        // preferences: don't show undo dialog
        if (!getDefaultSharedPreferences(getApplicationContext()).getBoolean("show_undo_dialog",
                getDefaultPreferencesValue(R.bool.show_undo_dialog_default))) {
            return;
        }

        // if undo dialog is already present, we can't handle it. Just return
        if (undoDialogFragment.isAdded()) {
            Timber.w("Can't handle an enqueue event while undo dialog is still visible");
            return;
        }

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
                try {
                    undoDialogFragment.dismiss();
                } catch (NullPointerException e) {
                    Timber.w(e, "Can't dismiss undo dialog");
                }
            }
        }, AUTO_DISMISS_MILLISECOND);
    }

    private void initMasterPlayerBehavior() {
        final Context context = getApplicationContext();
        playerEventListener = new MasterPlayerEventListener() {
            @Override
            public void onPlayStateChange() {

            }

            @Override
            public void onPlaylistChange() {
                updateControlBarVisibility();
            }

            @Override
            public void onCurrentTimeChange() {

            }

            @Override
            public void onPlaylistFinish() {
                // if the preferences says not to auto_enqueue
                if (!getDefaultSharedPreferences(context).getBoolean("auto_enqueue",
                        getDefaultPreferencesValue(R.bool.auto_enqueue_default))) {
                    return;
                }
                Playlist currentPlaylist = MasterPlayer.getInstance().getPlaylist();
                MasterPlayer.getInstance().enqueueRelated(
                        currentPlaylist.get(currentPlaylist.size() - 1), Position.Next);
            }
        };
        MasterPlayer.getInstance().addMasterPlayerEventListener(playerEventListener);
    }

    private void deInitMasterPlayerBehavior() {
        MasterPlayer.getInstance().removeMasterPlayerEventListener(playerEventListener);
    }

    private void updateControlBarVisibility() {
        if (MasterPlayer.getInstance().getPlaylist().isEmpty()) {
            playerControlsParentLayout.setVisibility(View.GONE);
            return;
        }
        playerControlsParentLayout.setVisibility(View.VISIBLE);
    }

    private void initTitleClickEvent() {
        // from http://stackoverflow.com/questions/24838155/set-onclick-listener-on-action-bar-title-in-android
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Disable the default and enable the custom
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowCustomEnabled(true);
            View customView = getLayoutInflater().inflate(R.layout.actionbar_title, null);
            // Get the textview of the title
            TextView customTitle = (TextView) customView.findViewById(R.id.actionbarTitle);

            // Set the on click listener for the title
            customTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    displayInitialFragment();
                }
            });
            // Apply the custom view
            actionBar.setCustomView(customView);
        }
    }

    private void initMotionEventHandler() {
        motionEventHandler = new MotionEventHandler();
        motionEventHandler.setOnViewClickListener(youtubeFragmentHolder, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayPlayerControls();
            }
        });
    }

    // from http://stackoverflow.com/questions/1109022/close-hide-the-android-soft-keyboard
    private void closeKeyboard() {
        View view = getCurrentFocus();
        if (view == null) {
            Timber.w("Can't close keyboard: current focus view not found");
            return;
        }
        InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(view.getWindowToken(), 0);

        // show player controls
        getSupportFragmentManager().beginTransaction().show(playerControls).commit();
    }

    private void displayFragment(Fragment fragment) {
        closeKeyboard();
        // don't show fragment if already visible
        if (fragment.isVisible()) {
            return;
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.activityMain_topPlaceholder, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void displayInitialFragment() {
        displayPopular();
    }

    private void displayPlaylist() {
        displayFragment(playlistFragment);
        closeSidePanel();
    }

    private void displaySearch() {
        displayFragment(searchFragment);
        closeSidePanel();
    }

    private void displayPopular() {
        displayFragment(popularFragment);
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

    private void displayPreference() {
        displayFragment(generalPreferenceFragment);
        closeSidePanel();
    }

    private void initTheme() {
        if (getDefaultSharedPreferences(this).getBoolean("dark_theme",
                getDefaultPreferencesValue(R.bool.dark_theme_default))) {
            setTheme(R.style.AppTheme_Dark);
            currentThemeIsDark = true;
            return;
        }
        setTheme(R.style.AppTheme_Light);
        currentThemeIsDark = false;
    }

    private void initPreferencesListener() {
        final boolean isCurrentThemeDark = currentThemeIsDark;
        preferencesListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                GeneralPreferencesEnum enumKey = GeneralPreferencesEnum.fromString(key);
                switch (enumKey) {
                    case DARK_THEME:
                        boolean darkTheme = sharedPreferences.getBoolean(key,
                                getDefaultPreferencesValue(R.bool.dark_theme_default));
                        // if the theme is already the right one, no need to restart
                        if ((darkTheme && isCurrentThemeDark) || (!darkTheme && !isCurrentThemeDark)) {
                            return;
                        }
                        darkThemePreferenceChanged();
                        break;
                    case DISABLE_SCREEN_ROTATION:
                        updateScreenRotationPreference();
                        break;
                }
            }
        };
        getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(preferencesListener);
    }

    private void darkThemePreferenceChanged() {
        new AlertDialog.Builder(this)
                .setTitle("Restart ?")
                .setMessage("To change the theme, we need to restart the application. Do you want to do it now ?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                    }
                }).create().show();
    }

    private void updateScreenRotationPreference() {
        boolean screenRotationIsDisabled =
                getDefaultSharedPreferences(this).getBoolean("disable_screen_rotation",
                        getDefaultPreferencesValue(R.bool.disable_screen_rotation_default));
        if (screenRotationIsDisabled) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
            return;
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }


    private void deinitPreferencesListener() {
        getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(preferencesListener);
    }
}
