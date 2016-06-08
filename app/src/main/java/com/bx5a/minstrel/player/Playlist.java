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

import java.util.ArrayList;

/**
 * Defines a basic operation that can be done on a list of playable
 */
public class Playlist {
    public interface EventListener {
        void onChanged();
    }

    private ArrayList<Playable> list;
    private EventListener eventListener;

    Playlist() {
        list = new ArrayList<>();
        eventListener = new EventListener() {
            @Override
            public void onChanged() {

            }
        };
    }

    public ArrayList<Playable> getList() {
        return list;
    }

    public void enqueue(Playable obj, int index) throws IndexOutOfBoundsException {
        if (index < 0 || index > list.size()) {
            throw new IndexOutOfBoundsException("Invalid index " + String.valueOf(index));
        }
        list.add(index, obj);
        eventListener.onChanged();
    }

    public void remove(int index) throws IndexOutOfBoundsException {
        if (!hasIndex(index)) {
            throw new IndexOutOfBoundsException("Invalid index " + String.valueOf(index));
        }
        list.remove(index);
        eventListener.onChanged();
    }
    
    public Playable get(int index) throws IndexOutOfBoundsException {
        if (!hasIndex(index)) {
            throw new IndexOutOfBoundsException("Invalid index " + String.valueOf(index));
        }
        return list.get(index);
    }

    public void reorder(int sourceIndex, int destinationIndex) throws IndexOutOfBoundsException {
        if (!hasIndex(sourceIndex) || !hasIndex(destinationIndex)) {
            throw new IndexOutOfBoundsException("Invalid index");
        }
        int afterRemoveDestinationIndex = destinationIndex;
        if (sourceIndex < destinationIndex) {
            afterRemoveDestinationIndex--;
        }

        Playable playable = list.get(sourceIndex);
        list.remove(sourceIndex);
        list.add(afterRemoveDestinationIndex, playable);
        eventListener.onChanged();
    }

    public int size() {
        return list.size();
    }

    public void setEventListener(EventListener eventListener) {
        this.eventListener = eventListener;
    }

    public boolean hasIndex(int index) {
        return index >= 0 && index < size();
    }

    public boolean isEmpty() {
        return size() == 0;
    }
}
