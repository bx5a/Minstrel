package com.bx5a.minstrel.widget;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.bx5a.minstrel.R;

/**
 * Created by guillaume on 01/06/2016.
 */
public class AboutFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View view = layoutInflater.inflate(R.layout.fragment_about, null);

        Button githubButton = (Button) view.findViewById(R.id.fragmentAbout_githubButton);
        githubButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://github.com/bx5a/Minstrel");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        // TODO: understand why those 2 lines are necessary to match parent
        LinearLayout rootLayout = (LinearLayout) view.findViewById(R.id.fragmentAbout_rootLayout);
        rootLayout.setLayoutParams(
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT));

        return view;
    }
}
