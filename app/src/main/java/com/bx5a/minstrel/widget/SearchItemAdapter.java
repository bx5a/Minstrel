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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bx5a.minstrel.R;
import com.bx5a.minstrel.utils.ThumbnailManager;
import com.bx5a.minstrel.youtube.YoutubeVideo;

import java.util.List;

/**
 * Adapter used by the search fragment
 */
public class SearchItemAdapter extends ArrayAdapter<YoutubeVideo> {

    Context context;

    public SearchItemAdapter(Context context, List<YoutubeVideo> videos) {
        super(context, -1, videos);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater layoutInflater =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.listitem_search, null);
        }
        YoutubeVideo video = getItem(position);

        view.setTag(video);

        TextView title = (TextView) view.findViewById(R.id.listItemVideo_title);
        TextView viewCount = (TextView) view.findViewById(R.id.listItemVideo_viewCount);
        final ImageView thumbnail = (ImageView) view.findViewById(R.id.listItemVideo_thumbnail);

        title.setText(video.getTitle());
        viewCount.setText(
                String.format(context.getString(R.string.view_count), video.getViewCount()));

        ThumbnailManager.getInstance().retreive(video.getThumbnailURL(), new ThumbnailManager.BitmapAvailableListener() {
            @Override
            public void onBitmapAvailable(Bitmap bitmap) {
                thumbnail.setImageBitmap(bitmap);
            }
        });

        return view;
    }
}
