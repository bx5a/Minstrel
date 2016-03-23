package com.bx5a.minstrel;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by guillaume on 22/03/2016.
 */
public class VideoAdapter extends ArrayAdapter<Video> {

    Context context;

    public VideoAdapter(Context context, List<Video> videos) {
        super(context, -1, videos);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater layoutInflater =
                    (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.listitem_video, null);
        }
        Video video = getItem(position);

        view.setTag(video);

        TextView title = (TextView)view.findViewById(R.id.listItemVideo_title);
        TextView description = (TextView)view.findViewById(R.id.listItemVideo_description);

        title.setText(video.getTitle());
        description.setText(video.getDescription());

        return view;
    }
}
