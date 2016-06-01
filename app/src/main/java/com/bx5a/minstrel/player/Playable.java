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

import java.io.IOException;
import java.util.List;

public interface Playable {
    // Let you initialize only for its id
    void initFromId(String id) throws IOException;

    void load() throws IllegalStateException;

    boolean isLoaded();

    void play() throws IllegalStateException;

    void pause() throws IllegalStateException;

    String title();

    // position is a value [0, 1]. 0 being the beginning of the song and 1 the end
    void seekTo(float position) throws IllegalStateException;

    String getId();

    // can return an empty string if no thumbnail available
    String getThumbnailURL();

    String duration();

    Player getPlayer();

    interface RelatedAvailableListener {
        void onRelatedAvailable(List<Playable> related);
    }
    void asyncGetRelated(RelatedAvailableListener eventListener);
}
