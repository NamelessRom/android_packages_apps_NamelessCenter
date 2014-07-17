package org.namelessrom.center.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGBuilder;

import org.namelessrom.center.AppInstance;

/**
 * Makes our life easier at working with {@link android.graphics.drawable.Drawable}s
 */
public class DrawableHelper {

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
                .readFromResource(AppInstance.applicationContext.getResources(), rawId)
                .build()
                .getDrawable();
        drawable.setDither(true);
        return drawable;
    }

}
