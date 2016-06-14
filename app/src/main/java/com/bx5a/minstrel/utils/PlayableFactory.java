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

package com.bx5a.minstrel.utils;

import com.bx5a.minstrel.exception.ClassNotRegisteredException;
import com.bx5a.minstrel.exception.PlayableCreationException;
import com.bx5a.minstrel.player.Playable;
import com.bx5a.minstrel.youtube.YoutubeVideo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Used to initialize a playable using it class name and an id
 */
public class PlayableFactory {
    public interface PlayableCreator {
        Playable create(String id) throws PlayableCreationException;
        List<Playable> createList(List<String> ids) throws PlayableCreationException;
    }

    private static PlayableFactory ourInstance = new PlayableFactory();

    public static PlayableFactory getInstance() {
        return ourInstance;
    }

    private Map<String, PlayableCreator> playableCreatorMap;

    private PlayableFactory() {
        playableCreatorMap = new HashMap<>();
        registerPlayableClasses();
    }

    /**
     * That function is necessary but disgusting.
     * To execute the static {} piece of code in java, we are required to load the class.
     * To allow a class to register itself to the factory, we need that static part of the class to
     * be executed.
     * To force the loading of the class before using the create function, we need to call at least
     * one function on that class.
     * To keep the system simple, we force the user to add a call to registerToFactory on every
     * subclass of Playable in that function
     */
    private void registerPlayableClasses() {
        YoutubeVideo.registerToFactory(this);
    }

    public void register(PlayableCreator playableCreator, String className) {
        playableCreatorMap.put(className, playableCreator);
    }

    public Playable create(String className, String id)
            throws ClassNotRegisteredException, PlayableCreationException {
        if (!playableCreatorMap.containsKey(className)) {
            throw new ClassNotRegisteredException(
                    className + " Not found. Make sure that class is added in the registerPlayableClasses() function");
        }
        return playableCreatorMap.get(className).create(id);
    }

    public List<Playable> createList(String className, List<String> ids)
            throws ClassNotRegisteredException, PlayableCreationException {
        if (!playableCreatorMap.containsKey(className)) {
            throw new ClassNotRegisteredException(
                    className + " Not found. Make sure that class is added in the registerPlayableClasses() function");
        }

        return playableCreatorMap.get(className).createList(ids);
    }
}
