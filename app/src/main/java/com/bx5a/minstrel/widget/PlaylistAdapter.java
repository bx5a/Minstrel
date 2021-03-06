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

package com.bx5a.minstrel.widget;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bx5a.minstrel.R;
import com.bx5a.minstrel.player.MasterPlayer;
import com.bx5a.minstrel.player.Playable;
import com.bx5a.minstrel.player.Playlist;

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

        duration.setTextColor(getDefaultTextColor());
        title.setTextColor(getDefaultTextColor());
        view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.transparent));
        if (MasterPlayer.getInstance().getCurrentPlayableIndex() == position) {
            duration.setTextColor(ContextCompat.getColor(getContext(), R.color.darkGray));
            title.setTextColor(ContextCompat.getColor(getContext(), R.color.darkGray));
            view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.orange));
        }

        title.setText(playable.getTitle());
        duration.setText(playable.getDuration());

        return view;
    }

    private int getDefaultTextColor() {
        TypedValue value = new TypedValue();
        getContext().getTheme().resolveAttribute(android.R.attr.textColorSecondary, value, true);
        return value.data;
    }
}
