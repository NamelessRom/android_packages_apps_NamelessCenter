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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.larvalabs.svgandroid.SVGBuilder;

import org.namelessrom.center.AppInstance;

/**
 * Makes our life easier at working with {@link android.graphics.drawable.Drawable}s
 */
public class DrawableHelper {

    public static Bitmap drawableToBitmap(final int resId) {
        return drawableToBitmap(AppInstance.get().getResources().getDrawable(resId));
    }

    public static Bitmap drawableToBitmap(final Drawable drawable) {
        if (drawable == null) return null;

        if (drawable instanceof BitmapDrawable) return ((BitmapDrawable) drawable).getBitmap();

        final Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public static Drawable getSvgDrawable(final int rawId) {
        final Drawable drawable = new SVGBuilder()
                .readFromResource(AppInstance.get().getResources(), rawId)
                .build()
                .getDrawable();
        drawable.setDither(true);
        return drawable;
    }

}
