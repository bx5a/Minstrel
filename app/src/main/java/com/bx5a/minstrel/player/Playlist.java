package com.bx5a.minstrel.player;

import java.util.ArrayList;

/**
 * Created by guillaume on 16/04/2016.
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
        list.add(index, obj);
        eventListener.onChanged();
    }

    public void remove(int index) throws IndexOutOfBoundsException {
        list.remove(index);
        eventListener.onChanged();
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
        eventListener.onChanged();
    }

    public int size() {
        return list.size();
    }

    public void setEventListener(EventListener eventListener) {
        this.eventListener = eventListener;
    }
}
