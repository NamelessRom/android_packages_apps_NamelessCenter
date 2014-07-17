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
