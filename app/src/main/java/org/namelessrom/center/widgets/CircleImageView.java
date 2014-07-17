package org.namelessrom.center.widgets;

import android.animation.ObjectAnimator;
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
import android.view.MotionEvent;
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

        final TypedArray attributes =
                context.obtainStyledAttributes(attrs, R.styleable.CircleImageView, defStyle, 0);

        try {
            hasBorder = attributes.getBoolean(R.styleable.CircleImageView_border, false);

            if (hasBorder) {
                final int defaultBorderSize =
                        ((int) (2 * context.getResources().getDisplayMetrics().density + 0.5f));
                setBorderWidth(attributes.getDimensionPixelOffset(
                        R.styleable.CircleImageView_border_width, defaultBorderSize));
                setBorderColor(attributes.getColor(
                        R.styleable.CircleImageView_border_color, Color.WHITE));
            }

            if (attributes.getBoolean(R.styleable.CircleImageView_shadow, false)) addShadow();
        } finally {
            attributes.recycle();
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

        int outerWidth = 0;
        int center = canvasSize / 2;

        if (hasBorder) {
            outerWidth = borderWidth;
            center = (canvasSize - (outerWidth * 2)) / 2;

            paint.setColorFilter(null);
            canvas.drawCircle(center + outerWidth, center + outerWidth,
                    ((canvasSize - (outerWidth * 2)) / 2) + outerWidth - SHADOW_RADIUS,
                    paintBorder);
        } else {
            paint.setColorFilter(null);
        }

        canvas.drawCircle(center + outerWidth, center + outerWidth,
                ((canvasSize - (outerWidth * 2)) / 2) - SHADOW_RADIUS, paint);
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
