package com.bx5a.minstrel.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bx5a.minstrel.R;
import com.bx5a.minstrel.player.Playable;
import com.bx5a.minstrel.player.Player;

import java.util.List;

/**
 * Created by guillaume on 13/04/2016.
 */
public class PlayableAdapter extends ArrayAdapter<Playable> {
    private Context context;

    public PlayableAdapter(Context context, List<Playable> playables) {
        super(context, -1, playables);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater layoutInflater =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.listitem_playable, null);
        }
        Playable playable = getItem(position);

        view.setTag(playable);

        TextView title = (TextView) view.findViewById(R.id.listItemPlayable_title);
        if (Player.getInstance().getCurrentPlayableIndex() == position) {
            title.setTypeface(title.getTypeface(), Typeface.BOLD_ITALIC);
        }

        title.setText(playable.title());

        return view;
    }
}
