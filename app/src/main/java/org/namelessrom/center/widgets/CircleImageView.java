package org.namelessrom.center.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
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

    private boolean isSelected;
    private int     canvasSize;

    private boolean hasBorder;
    private int     borderWidth;

    private boolean hasSelector;
    private int     selectorStrokeWidth;

    private BitmapShader shader;
    private Bitmap       bitmap;

    private Paint paint;
    private Paint paintBorder;
    private Paint paintSelectorBorder;

    private ColorFilter selectorFilter;

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
        paintSelectorBorder = new Paint();
        paintSelectorBorder.setAntiAlias(true);

        final TypedArray attributes =
                context.obtainStyledAttributes(attrs, R.styleable.CircleImageView, defStyle, 0);

        try {
            hasBorder = attributes.getBoolean(R.styleable.CircleImageView_border, false);
            hasSelector = attributes.getBoolean(R.styleable.CircleImageView_selector, false);

            if (hasBorder) {
                final int defaultBorderSize =
                        ((int) (2 * context.getResources().getDisplayMetrics().density + 0.5f));
                setBorderWidth(attributes.getDimensionPixelOffset(
                        R.styleable.CircleImageView_border_width, defaultBorderSize));
                setBorderColor(attributes.getColor(
                        R.styleable.CircleImageView_border_color, Color.WHITE));
            }

            if (hasSelector) {
                final int defaultSelectorSize =
                        ((int) (2 * context.getResources().getDisplayMetrics().density + 0.5f));
                setSelectorStrokeWidth(attributes.getDimensionPixelOffset(
                        R.styleable.CircleImageView_selector_stroke_width, defaultSelectorSize));
                setSelectorStrokeColor(attributes.getColor(
                        R.styleable.CircleImageView_selector_stroke_color, Color.BLUE));
                setSelectorColor(attributes.getColor(
                        R.styleable.CircleImageView_selector_color, Color.TRANSPARENT));
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

    public void setSelectorColor(final int selectorColor) {
        this.selectorFilter = new PorterDuffColorFilter(selectorColor, PorterDuff.Mode.SRC_ATOP);
        this.invalidate();
    }

    public void setSelectorStrokeWidth(final int selectorStrokeWidth) {
        this.selectorStrokeWidth = selectorStrokeWidth;
        this.requestLayout();
        this.invalidate();
    }

    public void setSelectorStrokeColor(final int selectorStrokeColor) {
        if (paintSelectorBorder != null) paintSelectorBorder.setColor(selectorStrokeColor);
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

        if (hasSelector && isSelected) {
            outerWidth = selectorStrokeWidth;
            center = (canvasSize - (outerWidth * 2)) / 2;

            paint.setColorFilter(selectorFilter);
            canvas.drawCircle(center + outerWidth, center + outerWidth,
                    ((canvasSize - (outerWidth * 2)) / 2) + outerWidth - SHADOW_RADIUS,
                    paintSelectorBorder);
        } else if (hasBorder) {
            outerWidth = borderWidth;
            center = (canvasSize - (outerWidth * 2)) / 2;

            paint.setColorFilter(null);
            canvas.drawCircle(center + outerWidth, center + outerWidth,
                    ((canvasSize - (outerWidth * 2)) / 2) + outerWidth - SHADOW_RADIUS,
                    paintBorder);
        } else { paint.setColorFilter(null); }

        canvas.drawCircle(center + outerWidth, center + outerWidth,
                ((canvasSize - (outerWidth * 2)) / 2) - SHADOW_RADIUS, paint);
    }

    @Override
    public boolean dispatchTouchEvent(@NonNull final MotionEvent event) {
        if (!isClickable()) {
            isSelected = false;
            return super.onTouchEvent(event);
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isSelected = true;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_SCROLL:
            case MotionEvent.ACTION_OUTSIDE:
            case MotionEvent.ACTION_CANCEL:
                isSelected = false;
                break;
        }

        invalidate();
        return super.dispatchTouchEvent(event);
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

    public boolean isSelected() { return isSelected; }
}
