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

import java.security.InvalidParameterException;

/**
 * Created by guillaume on 10/06/2016.
 */
public enum GeneralPreferencesEnum {
    DARK_THEME,
    BRIGHTNESS_MANAGEMENT,
    DISABLE_SCREEN_ROTATION,
    AUTO_ENQUEUE;

    static public GeneralPreferencesEnum fromString(String value) throws InvalidParameterException {
        if (value.equals("dark_theme")) {
            return DARK_THEME;
        } else if (value.equals("brightness_management")) {
            return BRIGHTNESS_MANAGEMENT;
        } else if (value.equals("disable_screen_rotation")) {
            return DISABLE_SCREEN_ROTATION;
        } else if (value.equals("auto_enqueue")) {
            return AUTO_ENQUEUE;
        }
        throw new InvalidParameterException("Can't convert string " + value + " to enum");
    }
}
