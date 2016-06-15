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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bx5a.minstrel.R;
import com.bx5a.minstrel.player.Playable;
import com.bx5a.minstrel.utils.SearchList;
import com.bx5a.minstrel.youtube.YoutubeSearchEngine;
import com.bx5a.minstrel.youtube.YoutubeVideo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by guillaume on 10/06/2016.
 */
public class PopularFragment extends ThumbnailPlayableListFragment {
    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View view = super.onCreateView(layoutInflater, viewGroup, bundle);
        setTitle(R.string.popular);

        return view;
    }

    @Override
    protected SearchList<Playable> getSearchList() {
        if (!YoutubeSearchEngine.getInstance().isInitialized()) {
            YoutubeSearchEngine.getInstance().init(getContext());
        }
        try {
            return new YoutubeSearchList(YoutubeSearchEngine.getInstance().getPopularVideos());
        } catch (IOException e) {
            Timber.e(e, "Can't get youtube popular videos");
            return null;
        }
    }

    private class YoutubeSearchList implements SearchList<Playable> {
        private SearchList<YoutubeVideo> searchList;
        public YoutubeSearchList(SearchList<YoutubeVideo> searchList) {
            this.searchList = searchList;
        }
        @Override
        public void setMaxResults(long maxResults) {
            searchList.setMaxResults(maxResults);
        }

        @Override
        public List<Playable> getNextPage() throws IOException {
            List<YoutubeVideo> videos = searchList.getNextPage();
            ArrayList<Playable> playable = new ArrayList<>();
            playable.addAll(videos);
            return playable;
        }

        @Override
        public boolean hasNextPage() {
            return searchList.hasNextPage();
        }
    }
}
