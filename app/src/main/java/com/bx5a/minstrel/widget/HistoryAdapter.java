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
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bx5a.minstrel.R;
import com.bx5a.minstrel.exception.NoThumbnailAvailableException;
import com.bx5a.minstrel.player.Playable;
import com.bx5a.minstrel.utils.ThumbnailManager;

import java.util.List;

/**
 * Adapter used in the History fragment
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
        final Playable playable = getItem(position);

        view.setTag(playable);

        TextView title = (TextView) view.findViewById(R.id.listItemHistory_title);
        title.setText(playable.getTitle());
        final ImageView image = (ImageView) view.findViewById(R.id.listItemHistory_thumbnail);

        // display thumbnail
        try {
            ThumbnailManager.getInstance().retreive(playable.getThumbnailURL(),
                    new ThumbnailManager.BitmapAvailableListener() {
                @Override
                public void onBitmapAvailable(Bitmap bitmap) {
                    image.setImageBitmap(bitmap);
                }
            });
        } catch (NoThumbnailAvailableException e) {
            Log.w("HistoryAdapter", "Can't access thrumbnail for playable " + playable.getTitle());
            e.printStackTrace();
        }

        return view;
    }
}
