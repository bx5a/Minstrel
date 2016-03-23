package com.bx5a.minstrel;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Fill side panel
        ListView sidePanel = (ListView)findViewById(R.id.activityMain_sidePanel);
        String[] sidePanelItems = getResources().getStringArray(R.array.sidePanel_items);
        sidePanel.setAdapter(
                new ArrayAdapter<String>(this, R.layout.listitem_drawer, sidePanelItems));
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

    private void startSearchView() {
        Intent intent = new Intent(this, SearchActivity.class);
        startActivity(intent);
    }
}
