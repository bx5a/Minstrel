package com.bx5a.minstrel.player;

import java.security.InvalidParameterException;

/**
 * Created by guillaume on 30/05/2016.
 */
// the playlist manager represents common action you can made to a playlist when selected as
// currently played
public class PlaylistManager {
    private Playlist playlist;
    private int selectedIndex;

    public PlaylistManager(Playlist playlist) {
        this.playlist = playlist;
        this.selectedIndex = 0;
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
    }

    public void remove(int index) throws IndexOutOfBoundsException {
        playlist.remove(index);
        if (index != selectedIndex) {
            return;
        }
        selectedIndex = getValidGetIndexFromPosition(Position.Next);
    }

    public void reorder(int sourceIndex, Position destinationPosition) throws IndexOutOfBoundsException {
        int destinationIndex = getValidEnqueueIndexFromPosition(destinationPosition);
        playlist.reorder(sourceIndex, destinationIndex);

        // convert destination from enqueue index to get index
        destinationIndex = Math.max(destinationIndex - 1, 0);

        if (sourceIndex == selectedIndex) {
            selectedIndex = destinationIndex;
        } else if (destinationIndex == selectedIndex) {
            selectedIndex--;
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
}
