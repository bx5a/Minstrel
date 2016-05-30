package com.bx5a.minstrel.player;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by guillaume on 16/04/2016.
 */
public class Playlist {
    private ArrayList<Playable> list;

    Playlist() {
        list = new ArrayList<>();
    }

    public ArrayList<Playable> getList() {
        return list;
    }

    public void enqueue(Playable obj, int index) throws IndexOutOfBoundsException {
        list.add(index, obj);
    }

    public void remove(int index) throws IndexOutOfBoundsException {
        list.remove(index);
    }
    
    public Playable get(int index) throws IndexOutOfBoundsException {
        return list.get(index);
    }

    public void reorder(int sourceIndex, int destinationIndex) throws IndexOutOfBoundsException {
        int afterRemoveDestinationIndex = destinationIndex;
        if (sourceIndex < destinationIndex) {
            afterRemoveDestinationIndex--;
        }

        Playable playable = list.get(sourceIndex);
        list.remove(sourceIndex);
        list.add(afterRemoveDestinationIndex, playable);
    }

    public int size() {
        return list.size();
    }
}
