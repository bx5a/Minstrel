package com.bx5a.minstrel.youtube;

/**
 * Created by guillaume on 11/04/2016.
 */
public class Player {
    private static Player ourInstance = new Player();

    public static Player getInstance() {
        return ourInstance;
    }

    private Player() {
    }
}
