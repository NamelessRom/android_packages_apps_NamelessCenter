/*
 * <!--
 *    Copyright (C) 2014 Alexander "Evisceration" Martinz
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * -->
 */

package org.namelessrom.center.utils;

import android.view.View;

/**
 * A class for easy modifying of views
 */
public class ViewHelper {

    public static float getAlpha(final View view) { return view.getAlpha(); }

    public static void setAlpha(final View view, final float alpha) {
        view.setAlpha(alpha);
    }

    public static float getScaleX(final View view) { return view.getScaleX(); }

    public static void setScaleX(final View view, final float scaleX) {
        view.setScaleX(scaleX);
    }

    public static float getScaleY(final View view) { return view.getScaleY(); }

    public static void setScaleY(final View view, final float scaleY) {
        view.setScaleY(scaleY);
    }

    public static float getPivotX(final View view) { return view.getPivotX(); }

    public static void setPivotX(final View view, final float pivotX) {
        view.setPivotX(pivotX);
    }

    public static float getPivotY(final View view) { return view.getPivotY(); }

    public static void setPivotY(final View view, final float pivotY) {
        view.setPivotY(pivotY);
    }

}
