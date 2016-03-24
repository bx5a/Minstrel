package com.bx5a.minstrel;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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
        TextView viewCount = (TextView)view.findViewById(R.id.listItemVideo_viewCount);
        ImageView thumbnail = (ImageView)view.findViewById(R.id.listItemVideo_thumbnail);

        title.setText(video.getTitle());
        viewCount.setText(
                String.format(context.getString(R.string.view_count), video.getViewCount()));
        new DisplayImageTask(video.getThumbnailURL(),thumbnail).execute();

        return view;
    }

    // Class that enable async loading of image views
    // TODO: move to utils package ?
    class DisplayImageTask extends AsyncTask<Void, Void, Bitmap> {
        private String url;
        private ImageView imageView;

        public DisplayImageTask(String url, ImageView imageView) {
            this.url = url;
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                URL urlConnection = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) urlConnection.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            imageView.setImageBitmap(result);
        }
    }
}
