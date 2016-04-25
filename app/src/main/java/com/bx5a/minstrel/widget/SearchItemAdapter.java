package com.bx5a.minstrel.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bx5a.minstrel.R;
import com.bx5a.minstrel.youtube.YoutubeVideo;
import com.bx5a.minstrel.utils.DisplayImageTask;

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
            view = layoutInflater.inflate(R.layout.listitem_video, null);
        }
        YoutubeVideo video = getItem(position);

        view.setTag(video);

        TextView title = (TextView) view.findViewById(R.id.listItemVideo_title);
        TextView viewCount = (TextView) view.findViewById(R.id.listItemVideo_viewCount);
        ImageView thumbnail = (ImageView) view.findViewById(R.id.listItemVideo_thumbnail);

        title.setText(video.getTitle());
        viewCount.setText(
                String.format(context.getString(R.string.view_count), video.getViewCount()));
        new DisplayImageTask(video.getThumbnailURL(), thumbnail).execute();

        return view;
    }
}
