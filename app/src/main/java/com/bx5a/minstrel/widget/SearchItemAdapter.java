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
import com.bx5a.minstrel.youtube.ThumbnailManager;
import com.bx5a.minstrel.youtube.YoutubeVideo;

import java.util.List;

/**
 * Created by guillaume on 22/03/2016.
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
