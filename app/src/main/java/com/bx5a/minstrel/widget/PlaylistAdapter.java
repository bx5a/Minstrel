package com.bx5a.minstrel.widget;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bx5a.minstrel.R;
import com.bx5a.minstrel.player.Playable;
import com.bx5a.minstrel.player.MasterPlayer;
import com.bx5a.minstrel.player.Playlist;

import java.util.ArrayList;

/**
 * Created by guillaume on 13/04/2016.
 */
public class PlaylistAdapter extends ArrayAdapter<Playable> {
    private Context context;

    public PlaylistAdapter(Context context, Playlist playlist) {
        super(context, -1, playlist.getList());
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater layoutInflater =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.listitem_playlist, null);
        }
        Playable playable = getItem(position);

        view.setTag(playable);

        TextView title = (TextView) view.findViewById(R.id.listItemPlayable_title);
        TextView duration = (TextView) view.findViewById(R.id.listItemPlayable_duration);
        if (MasterPlayer.getInstance().getCurrentPlayableIndex() == position) {
            duration.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
            title.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
            view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
        } else {
            duration.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
            title.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
            view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
        }

        title.setText(playable.title());
        duration.setText(playable.duration());

        return view;
    }
}
