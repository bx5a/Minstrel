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

package com.bx5a.minstrel.player;

import com.bx5a.minstrel.exception.NoThumbnailAvailableException;

import java.util.List;

public interface Playable {
    String getClassName();

    void play();
    void pause();
    /**
     * Seek to the given position
     * @param position is a value in the [0, 1] interval
     */
    void seekTo(float position);

    String getTitle();
    String getId();
    String getThumbnailURL() throws NoThumbnailAvailableException;
    String getHighResolutionThumbnailURL() throws NoThumbnailAvailableException;
    String getDuration();

    Player getPlayer();

    interface RelatedAvailableListener {
        void onRelatedAvailable(List<Playable> related);
    }

    /**
     * Asynchronously get a list of related songs
     * @param eventListener
     */
    void asyncGetRelated(RelatedAvailableListener eventListener);

    boolean isEqual(Playable playable);
}
