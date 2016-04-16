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

    // Adds at last position
    public void add(Playable obj) {
        list.add(obj);
    }

    // similar to ArrayList add function
    public void add(Playable obj, int index) throws IndexOutOfBoundsException {
        if (index < 0 || index > list.size()) {
            throw new IndexOutOfBoundsException("Can't insert at index " + index + " of playlist");
        }
        list.add(index, obj);
    }

    public void remove(int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= list.size()) {
            throw new IndexOutOfBoundsException("Can't get index " + index + " of playlist");
        }
        list.remove(index);
    }
    
    public Playable at(int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= list.size()) {
            throw new IndexOutOfBoundsException("Can't get index " + index + " of playlist");
        }
        return list.get(index);
    }

    public int size() {
        return list.size();
    }
}
