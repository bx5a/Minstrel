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

package com.bx5a.minstrel.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.LruCache;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ThumbnailManager {
    private static ThumbnailManager ourInstance = new ThumbnailManager();

    public static ThumbnailManager getInstance() {
        return ourInstance;
    }
    private LruCache<String, Bitmap> cache;

    private ThumbnailManager() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 16;
        cache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    public interface BitmapAvailableListener {
        void onBitmapAvailable(Bitmap bitmap);
    }

    public void retreive(final String url, final BitmapAvailableListener listener) {
        // already in cache
        Bitmap result = cache.get(url);
        if (result != null) {
            listener.onBitmapAvailable(result);
            return;
        }
        // download bitmap if wasn't in cache
        DownloadThumbnailTask task = new DownloadThumbnailTask(url, new BitmapAvailableListener() {
            @Override
            public void onBitmapAvailable(Bitmap bitmap) {
                cache.put(url, bitmap);
                listener.onBitmapAvailable(bitmap);
            }
        });
        task.execute();
    }

    public class DownloadThumbnailTask extends AsyncTask<Void, Void, Bitmap> {
        private String url;
        private BitmapAvailableListener listener;

        public DownloadThumbnailTask(String url, BitmapAvailableListener listener) {
            this.url = url;
            this.listener = listener;
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
            listener.onBitmapAvailable(result);
        }
    }
}
