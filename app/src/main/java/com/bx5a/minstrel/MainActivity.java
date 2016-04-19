package com.bx5a.minstrel;

import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.bx5a.minstrel.player.MasterPlayer;
import com.bx5a.minstrel.widget.PlaylistFragment;
import com.bx5a.minstrel.youtube.DeveloperKey;
import com.bx5a.minstrel.youtube.YoutubePlayer;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = (DrawerLayout) findViewById(R.id.activityMain_drawer);

        // init master player
        MasterPlayer.getInstance().setContext(this);
        // init youtube using that popup
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

        // display playlist as default fragment
        // TODO: should be opened when something is added to the playlist or when the PlayerControlFragment is pressed
        displayPlaylist();
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
                startSearchView();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void startSearchView() {
        drawerLayout.openDrawer(Gravity.START);
    }

    private void displayPlaylist() {
        PlaylistFragment fragment = new PlaylistFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.activityMain_topPlaceholder, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
