package com.bx5a.minstrel.player;

import android.util.Log;

import java.security.InvalidParameterException;
import java.util.List;

/**
 * Created by guillaume on 30/05/2016.
 */
// the playlist manager represents common action you can made to a playlist when selected as
// currently played
public class PlaylistManager {
    public interface EventListener {
        void onEnqueued(int enqueuedIndex, int selectedIndex);
    }
    public interface SelectedIndexEventListener {
        void onChanged();
    }

    private Playlist playlist;
    private int selectedIndex;
    private EventListener eventListener;
    private SelectedIndexEventListener selectedIndexEventListener;

    public PlaylistManager(Playlist playlist) {
        this.playlist = playlist;
        this.selectedIndex = 0;
        this.eventListener = new EventListener() {
            @Override
            public void onEnqueued(int enqueuedIndex, int selectedIndex) {

            }
        };
        this.selectedIndexEventListener = new SelectedIndexEventListener() {
            @Override
            public void onChanged() {

            }
        };
    }

    // returns the expected index from position. Can be invalid (<0 or >= size())
    private int getIndexFromPosition(Position position) {
        switch (position) {
            case Current:
                return selectedIndex;
            case Next:
                return selectedIndex + 1;
            case Last:
                return playlist.size();

        }
        throw new InvalidParameterException("Unknown position");
    }

    // returns the valid (>=0 && <= size()) index from position
    private int getValidEnqueueIndexFromPosition(Position position) {
        return Math.max(Math.min(getIndexFromPosition(position), size()), 0);
    }
    // returns the valid (>=0 && < size()) index from position
    private int getValidGetIndexFromPosition(Position position) {
        return Math.max(Math.min(getIndexFromPosition(position), size() - 1), 0);
    }

    public void enqueue(Playable playable, Position position) {
        int index = getValidEnqueueIndexFromPosition(position);
        playlist.enqueue(playable, index);
        if (index <= selectedIndex) {
            move(Math.min(index + 1, size() - 1));
        }
        eventListener.onEnqueued(index, selectedIndex);
    }

    public void remove(int index) throws IndexOutOfBoundsException {
        playlist.remove(index);
        if (index < selectedIndex) {
            move(selectedIndex - 1);
        }

        if (index != selectedIndex) {
            return;
        }
        move(getValidGetIndexFromPosition(Position.Next));
    }

    public void reorder(int sourceIndex, Position destinationPosition) throws IndexOutOfBoundsException {
        int destinationIndex = getValidEnqueueIndexFromPosition(destinationPosition);
        if (sourceIndex == destinationIndex) {
            return;
        }

        playlist.reorder(sourceIndex, destinationIndex);

        // convert destination from enqueue index to get index
        destinationIndex = Math.min(destinationIndex, size() - 1);
        if (sourceIndex < selectedIndex) {
            move(selectedIndex - 1);
        } else if (sourceIndex == selectedIndex) {
            move(destinationIndex);
        } else if (destinationIndex == selectedIndex) {
            move(selectedIndex - 1);
        }
    }

    public Playable get(int index) throws IndexOutOfBoundsException {
        return playlist.get(index);
    }

    public int size() {
        return playlist.size();
    }

    public void next() throws IndexOutOfBoundsException {
        move(selectedIndex + 1);
    }

    public void previous() throws IndexOutOfBoundsException {
        move(selectedIndex - 1);
    }

    public void move(int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= playlist.size()) {
            throw new IndexOutOfBoundsException("index " + String.valueOf(index) + " invalid");
        }
        selectedIndex = index;
        selectedIndexEventListener.onChanged();
    }

    public Playable getSelected() {
        if (size() == 0) {
            return null;
        }
        return playlist.get(selectedIndex);
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public Playlist getPlaylist() {
        return playlist;
    }

    public void setEventListener(EventListener eventListener) {
        this.eventListener = eventListener;
    }

    public void setSelectedIndexEventListener(SelectedIndexEventListener selectedIndexEventListener) {
        this.selectedIndexEventListener = selectedIndexEventListener;
    }
}
