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

package org.namelessrom.center.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.ImageView;

import org.namelessrom.center.R;
import org.namelessrom.center.utils.DrawableHelper;

public class CircleImageView extends ImageView {

    private final float SHADOW_RADIUS = 10.0f;

    private int canvasSize;

    private boolean hasBorder;
    private int     borderWidth;

    private BitmapShader shader;
    private Bitmap       bitmap;

    private Paint paint;
    private Paint paintBorder;

    public CircleImageView(final Context context) { this(context, null); }

    public CircleImageView(final Context context, final AttributeSet attrs) {
        this(context, attrs, R.attr.circleImageViewStyle);
    }

    public CircleImageView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(final Context context, final AttributeSet attrs, final int defStyle) {
        paint = new Paint();
        paint.setAntiAlias(true);
        paintBorder = new Paint();
        paintBorder.setAntiAlias(true);

        final TypedArray a =
                context.obtainStyledAttributes(attrs, R.styleable.CircleImageView, defStyle, 0);

        try {
            hasBorder = a.getBoolean(R.styleable.CircleImageView_border, false);

            if (hasBorder) {
                final int defaultBorderSize =
                        ((int) (2 * context.getResources().getDisplayMetrics().density + 0.5f));
                setBorderWidth(a.getDimensionPixelOffset(
                        R.styleable.CircleImageView_border_width, defaultBorderSize));
                setBorderColor(a.getColor(R.styleable.CircleImageView_border_color, Color.WHITE));
            }

            if (a.getBoolean(R.styleable.CircleImageView_shadow, false)) addShadow();
        } finally {
            a.recycle();
        }
    }

    public void setBorderWidth(final int borderWidth) {
        this.borderWidth = borderWidth;
        this.requestLayout();
        this.invalidate();
    }

    public void setBorderColor(final int borderColor) {
        if (paintBorder != null) paintBorder.setColor(borderColor);
        this.invalidate();
    }

    public void addShadow() {
        setLayerType(LAYER_TYPE_SOFTWARE, paintBorder);
        paintBorder.setShadowLayer(SHADOW_RADIUS, 0.0f, 0.0f, Color.BLACK);
        this.invalidate();
    }

    @Override
    public void onDraw(@NonNull final Canvas canvas) {
        if (bitmap == null) return;
        if (bitmap.getHeight() == 0 || bitmap.getWidth() == 0) return;

        final int oldCanvasSize = canvasSize;

        canvasSize = canvas.getWidth();
        if (canvas.getHeight() < canvasSize) canvasSize = canvas.getHeight();

        if (oldCanvasSize != canvasSize) refreshBitmapShader();

        paint.setShader(shader);

        int center = canvasSize / 2;

        if (hasBorder) {
            center = (canvasSize - (borderWidth * 2)) / 2;

            canvas.drawCircle(center + borderWidth, center + borderWidth,
                    ((canvasSize - (borderWidth * 2)) / 2) + borderWidth - SHADOW_RADIUS,
                    paintBorder);
        }

        canvas.drawCircle(center + borderWidth, center + borderWidth,
                ((canvasSize - (borderWidth * 2)) / 2) - SHADOW_RADIUS, paint);
    }

    @Override
    public void invalidate(@NonNull final Rect dirty) {
        super.invalidate(dirty);
        bitmap = DrawableHelper.drawableToBitmap(getDrawable());
    }

    @Override
    public void invalidate(int l, int t, int r, int b) {
        super.invalidate(l, t, r, b);
        bitmap = DrawableHelper.drawableToBitmap(getDrawable());
    }

    @Override
    public void invalidate() {
        super.invalidate();
        bitmap = DrawableHelper.drawableToBitmap(getDrawable());
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
    }

    private int measureWidth(final int measureSpec) {
        final int result;
        final int specMode = MeasureSpec.getMode(measureSpec);

        if (specMode == MeasureSpec.EXACTLY || specMode == MeasureSpec.AT_MOST) {
            result = MeasureSpec.getSize(measureSpec);
        } else {
            result = canvasSize;
        }

        return result;
    }

    private int measureHeight(final int measureSpecHeight) {
        final int result;
        final int specMode = MeasureSpec.getMode(measureSpecHeight);

        if (specMode == MeasureSpec.EXACTLY || specMode == MeasureSpec.AT_MOST) {
            result = MeasureSpec.getSize(measureSpecHeight);
        } else {
            result = canvasSize;
        }

        return (result + 2);
    }

    public void refreshBitmapShader() {
        shader = new BitmapShader(
                Bitmap.createScaledBitmap(bitmap, canvasSize, canvasSize, false),
                Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
    }

}
