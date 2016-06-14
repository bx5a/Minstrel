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

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bx5a.minstrel.R;
import com.bx5a.minstrel.exception.PageNotAvailableException;
import com.bx5a.minstrel.player.Playable;
import com.bx5a.minstrel.utils.SearchList;
import com.bx5a.minstrel.youtube.YoutubeSearchEngine;
import com.bx5a.minstrel.youtube.YoutubeVideo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by guillaume on 10/06/2016.
 */
public class PopularFragment extends ThumbnailPlayableListFragment {
    private final long MAX_RESULT_NUMBER = 30;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View view = super.onCreateView(layoutInflater, viewGroup, bundle);
        setTitle(R.string.popular);

        return view;
    }

    @Override
    protected PlayablePages getPlayables() {
        return new PopularPages();
    }

    private class PopularPages implements PlayablePages {
        private SearchList<YoutubeVideo> youtubePopular;
        public PopularPages() {
            try {
                youtubePopular = YoutubeSearchEngine.getInstance().getPopularVideos();
                youtubePopular.setMaxResults(MAX_RESULT_NUMBER);
            } catch (IOException e) {
                Log.e("PopularFragment", "Can't get youtube popular videos: " + e.getMessage());
                e.printStackTrace();
                youtubePopular = null;
            }
        }

        @Override
        public boolean hastNextPlayablePage() {
            if (youtubePopular == null) {
                return false;
            }
            return youtubePopular.hasNextPage();
        }

        @Override
        public List<Playable> getNextPlayablePage() {
            if (!hastNextPlayablePage()) {
                throw new PageNotAvailableException("No next page available");
            }
            ArrayList<Playable> playables = new ArrayList<>();

            List<YoutubeVideo> videos;
            try {
                videos = youtubePopular.getNextPage();
            } catch (IOException e) {
                Log.e("PopularFragment", "Can't get popular videos: " + e.getMessage());
                e.printStackTrace();
                return playables;
            }

            for (YoutubeVideo video : videos) {
                playables.add(video);
            }
            return playables;
        }
    }
}
