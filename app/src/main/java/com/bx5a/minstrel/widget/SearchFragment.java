package com.bx5a.minstrel.widget;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bx5a.minstrel.R;

/**
 * Created by guillaume on 19/04/2016.
 */
public class SearchFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View view = layoutInflater.inflate(R.layout.fragment_search, null);
        return view;
    }
}
