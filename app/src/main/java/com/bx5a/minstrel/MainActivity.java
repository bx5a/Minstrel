package com.bx5a.minstrel;

import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.bx5a.minstrel.widget.PlaylistFragment;
import com.bx5a.minstrel.youtube.DeveloperKey;
import com.bx5a.minstrel.youtube.YoutubePlayer;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

public class MainActivity extends AppCompatActivity {

    private PopupWindow youtubeVideo;
    private View placeholder;
    private View popupView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        placeholder = findViewById(R.id.activityMain_videoPlaceholder);

        // init video popup
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        popupView = layoutInflater.inflate(R.layout.popup_youtubevideo, null);
        youtubeVideo = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        // init youtube using that popup
        YouTubePlayerSupportFragment fragment = (YouTubePlayerSupportFragment) getSupportFragmentManager().findFragmentById(R.id.youtube_fragment);
        fragment.initialize(DeveloperKey.DEVELOPER_KEY, new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                YoutubePlayer.getInstance().setYoutubePlayer(youTubePlayer);

                // pop at the right position
                youtubeVideo.showAsDropDown(placeholder, 0, 0);
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
        // TODO: pop side panel
    }

    private void displayPlaylist() {
        PlaylistFragment fragment = new PlaylistFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.activityMain_topPlaceholder, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
