package com.bx5a.minstrel.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bx5a.minstrel.R;
import com.bx5a.minstrel.player.Playable;

import java.util.List;

/**
 * Created by guillaume on 21/04/2016.
 */
public class HistoryAdapter extends ArrayAdapter<Playable> {
    private Context context;

    public HistoryAdapter(Context context, List<Playable> payableList) {
        super(context, -1, payableList);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater layoutInflater =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.listitem_history, null);
        }
        Playable playable = getItem(position);

        view.setTag(playable);

        TextView title = (TextView) view.findViewById(R.id.listItemHistory_title);
        title.setText(playable.title());

        return view;
    }
}
