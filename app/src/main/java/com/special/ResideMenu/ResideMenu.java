/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 SpecialCyCi
 * Modifications Copyright (c) 2014 Alexander "Evisceration" Martinz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.special.ResideMenu;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import org.namelessrom.center.R;
import org.namelessrom.center.utils.AnimationHelper;
import org.namelessrom.center.utils.ViewHelper;

import java.util.ArrayList;
import java.util.List;

import static butterknife.ButterKnife.findById;

public class ResideMenu extends FrameLayout {

    public static final int DIRECTION_LEFT  = 0;
    public static final int DIRECTION_RIGHT = 1;

    private static final int PRESSED_MOVE_HORIZONTAL = 2;
    private static final int PRESSED_DOWN            = 3;
    private static final int PRESSED_DONE            = 4;
    private static final int PRESSED_MOVE_VERTICAL   = 5;

    private static final float MAX_LEFT = 75.0f;

    private final DisplayMetrics displayMetrics         = new DisplayMetrics();
    private final List<Integer>  disabledSwipeDirection = new ArrayList<Integer>();

    private ImageView    imageViewShadow;
    private ImageView    imageViewBackground;
    private LinearLayout layoutLeftMenu;
    private LinearLayout layoutRightMenu;
    private ScrollView   scrollViewLeftMenu;
    private ScrollView   scrollViewRightMenu;
    private ScrollView   scrollViewMenu;

    private Activity         activity;
    private ViewGroup        viewDecor;
    private TouchDisableView viewActivity;

    private boolean isOpened;
    private float   shadowAdjustScaleX;
    private float   shadowAdjustScaleY;
    private float   lastActionDownX, lastActionDownY;

    private float mScaleValue = 0.5f;

    private List<View>           ignoredViews;
    private List<ResideMenuItem> leftMenuItems;
    private List<ResideMenuItem> rightMenuItems;

    private OnMenuListener menuListener;
    private float          lastRawX;
    private boolean isInIgnoredView = false;
    private int     scaleDirection  = DIRECTION_LEFT;
    private int     pressedState    = PRESSED_DOWN;

    //==============================================================================================
    // Initializing
    //==============================================================================================

    public ResideMenu(final Context context) {
        super(context);
        initViews(context);
    }

    private void initViews(final Context context) {
        View.inflate(context, R.layout.residemenu, this);

        scrollViewLeftMenu = findById(this, R.id.sv_left_menu);
        scrollViewRightMenu = findById(this, R.id.sv_right_menu);
        imageViewShadow = findById(this, R.id.iv_shadow);
        layoutLeftMenu = findById(this, R.id.layout_left_menu);
        layoutRightMenu = findById(this, R.id.layout_right_menu);
        imageViewBackground = findById(this, R.id.iv_background);
    }

    public void attachToActivity(final Activity activity) {
        this.activity = activity;
        initValue();
        setShadowAdjustScaleXByOrientation();
        viewDecor.addView(this, 0);
        setViewPadding();
    }

    private void initValue() {
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        leftMenuItems = new ArrayList<ResideMenuItem>();
        rightMenuItems = new ArrayList<ResideMenuItem>();
        ignoredViews = new ArrayList<View>();
        viewDecor = (ViewGroup) activity.getWindow().getDecorView();
        viewActivity = new TouchDisableView(activity);

        final View mContent = viewDecor.getChildAt(0);
        viewDecor.removeViewAt(0);
        viewActivity.setContent(mContent);
        addView(viewActivity);
    }

    public int getScreenHeight() { return displayMetrics.heightPixels; }

    public int getScreenWidth() { return displayMetrics.widthPixels; }

    //==============================================================================================
    // Menu
    //==============================================================================================

    public void addMenuItem(final ResideMenuItem menuItem, final int direction) {
        if (direction == DIRECTION_LEFT) {
            this.leftMenuItems.add(menuItem);
            layoutLeftMenu.addView(menuItem);
        } else {
            this.rightMenuItems.add(menuItem);
            layoutRightMenu.addView(menuItem);
        }
    }

    public void setMenuItems(final List<ResideMenuItem> menuItems, final int direction) {
        if (direction == DIRECTION_LEFT) {
            this.leftMenuItems = menuItems;
        } else {
            this.rightMenuItems = menuItems;
        }
        rebuildMenu();
    }

    public List<ResideMenuItem> getMenuItems(final int direction) {
        if (direction == DIRECTION_LEFT) { return leftMenuItems; } else { return rightMenuItems; }
    }

    private void rebuildMenu() {
        layoutLeftMenu.removeAllViews();
        layoutRightMenu.removeAllViews();
        for (final ResideMenuItem item : leftMenuItems) layoutLeftMenu.addView(item);
        for (final ResideMenuItem item : rightMenuItems) layoutRightMenu.addView(item);
    }

    public void setMenuListener(final OnMenuListener menuListener) {
        this.menuListener = menuListener;
    }

    public OnMenuListener getMenuListener() { return menuListener; }

    //==============================================================================================
    // Views
    //==============================================================================================

    private void setShadowAdjustScaleXByOrientation() {
        final int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            shadowAdjustScaleX = 0.034f;
            shadowAdjustScaleY = 0.12f;
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            shadowAdjustScaleX = 0.06f;
            shadowAdjustScaleY = 0.07f;
        }
    }

    public void setBackground(final int imageResrouce) {
        imageViewBackground.setImageResource(imageResrouce);
    }

    public void setShadowVisible(final boolean isVisible) {
        if (isVisible) {
            imageViewShadow.setImageResource(R.drawable.shadow);
        } else {
            imageViewShadow.setImageBitmap(null);
        }
    }

    private void setViewPadding() {
        this.setPadding(viewActivity.getPaddingLeft(),
                viewActivity.getPaddingTop(),
                viewActivity.getPaddingRight(),
                viewActivity.getPaddingBottom());
    }

    //==============================================================================================
    // Animation and Input
    //==============================================================================================

    @Override
    public boolean dispatchTouchEvent(@NonNull final MotionEvent ev) {
        final float currentActivityScaleX = ViewHelper.getScaleX(viewActivity);
        if (currentActivityScaleX == 1.0f) { setScaleDirectionByRawX(ev.getRawX()); }

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastActionDownX = ev.getX();
                lastActionDownY = ev.getY();
                isInIgnoredView = (isInIgnoredView(ev) && !isOpened());
                pressedState = PRESSED_DOWN;
                break;
            case MotionEvent.ACTION_MOVE:
                if (isInIgnoredView || isInDisableDirection(scaleDirection) ||
                        (!isOpened() && lastActionDownX > MAX_LEFT) ||
                        (pressedState != PRESSED_DOWN && pressedState != PRESSED_MOVE_HORIZONTAL)) {
                    break;
                }

                final int xOffset = (int) (ev.getX() - lastActionDownX);
                final int yOffset = (int) (ev.getY() - lastActionDownY);

                if (pressedState == PRESSED_DOWN) {
                    if (yOffset > 25 || yOffset < -25) {
                        pressedState = PRESSED_MOVE_VERTICAL;
                        break;
                    }
                    if (xOffset < -50 || xOffset > 50) {
                        pressedState = PRESSED_MOVE_HORIZONTAL;
                        ev.setAction(MotionEvent.ACTION_CANCEL);
                    }
                    break;
                }
                if (currentActivityScaleX < 0.95) {
                    scrollViewMenu.setVisibility(VISIBLE);
                }

                final float targetScale = getTargetScale(ev.getRawX());
                ViewHelper.setScaleX(viewActivity, targetScale);
                ViewHelper.setScaleY(viewActivity, targetScale);
                ViewHelper.setScaleX(imageViewShadow, targetScale + shadowAdjustScaleX);
                ViewHelper.setScaleY(imageViewShadow, targetScale + shadowAdjustScaleY);
                ViewHelper.setAlpha(scrollViewMenu, (1 - targetScale) * 2.0f);

                lastRawX = ev.getRawX();
                return true;
            case MotionEvent.ACTION_UP:
                if (isInIgnoredView) break;
                if (pressedState != PRESSED_MOVE_HORIZONTAL) break;

                pressedState = PRESSED_DONE;
                if (isOpened()) {
                    if (currentActivityScaleX > 0.56f) {
                        closeMenu();
                    } else {
                        openMenu(scaleDirection);
                    }
                } else {
                    if (currentActivityScaleX < 0.94f) {
                        openMenu(scaleDirection);
                    } else {
                        closeMenu();
                    }
                }
                break;
        }
        lastRawX = ev.getRawX();
        return super.dispatchTouchEvent(ev);
    }

    public void setSwipeDirectionDisable(final int direction) {
        disabledSwipeDirection.add(direction);
    }

    private boolean isInDisableDirection(final int direction) {
        return disabledSwipeDirection.contains(direction);
    }

    private void setScaleDirection(final int direction) {
        final int screenWidth = getScreenWidth();
        float pivotX;
        float pivotY = getScreenHeight() * 0.5f;

        if (direction == DIRECTION_LEFT) {
            scrollViewMenu = scrollViewLeftMenu;
            pivotX = screenWidth * 1.5f;
        } else {
            scrollViewMenu = scrollViewRightMenu;
            pivotX = screenWidth * -0.5f;
        }

        ViewHelper.setPivotX(viewActivity, pivotX);
        ViewHelper.setPivotY(viewActivity, pivotY);
        ViewHelper.setPivotX(imageViewShadow, pivotX);
        ViewHelper.setPivotY(imageViewShadow, pivotY);
        scaleDirection = direction;
    }

    private final Animator.AnimatorListener animationListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(final Animator animation) {
            if (isOpened()) {
                scrollViewMenu.setVisibility(VISIBLE);
                if (menuListener != null) { menuListener.openMenu(); }
            }
        }

        @Override
        public void onAnimationEnd(final Animator animation) {
            // reset the view;
            if (isOpened()) {
                viewActivity.setTouchDisable(true);
                viewActivity.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(final View view) {
                        if (isOpened()) closeMenu();
                    }
                });
            } else {
                viewActivity.setTouchDisable(false);
                viewActivity.setOnClickListener(null);
                scrollViewMenu.setVisibility(GONE);
                if (menuListener != null) { menuListener.closeMenu(); }
            }
        }

        @Override
        public void onAnimationCancel(Animator animation) { }

        @Override
        public void onAnimationRepeat(Animator animation) { }
    };

    private void setScaleDirectionByRawX(final float currentRawX) {
        if (currentRawX < lastRawX) {
            setScaleDirection(DIRECTION_RIGHT);
        } else {
            setScaleDirection(DIRECTION_LEFT);
        }
    }

    private float getTargetScale(final float currentRawX) {
        float scaleFloatX = ((currentRawX - lastRawX) / getScreenWidth()) * 0.75f;
        scaleFloatX = ((scaleDirection == DIRECTION_RIGHT) ? -scaleFloatX : scaleFloatX);

        float targetScale = ViewHelper.getScaleX(viewActivity) - scaleFloatX;
        targetScale = targetScale > 1.0f ? 1.0f : targetScale;
        targetScale = targetScale < 0.5f ? 0.5f : targetScale;
        return targetScale;
    }

    public void setScaleValue(final float scaleValue) { this.mScaleValue = scaleValue; }

    //==============================================================================================
    // Ignored Views
    //==============================================================================================

    public void addIgnoredView(View v) { ignoredViews.add(v); }

    public void removeIgnoredView(View v) { ignoredViews.remove(v); }

    public void clearIgnoredViewList() { ignoredViews.clear(); }

    private boolean isInIgnoredView(final MotionEvent ev) {
        final Rect rect = new Rect();
        for (final View v : ignoredViews) {
            v.getGlobalVisibleRect(rect);
            if (rect.contains((int) ev.getX(), (int) ev.getY())) return true;
        }
        return false;
    }

    //==============================================================================================
    // Methods
    //==============================================================================================

    public boolean isOpened() { return isOpened; }

    public void openMenu(final int direction) {
        setScaleDirection(direction);
        isOpened = true;

        final AnimatorSet scaleDown_activity = AnimationHelper.buildScaleDownAnimation(
                viewActivity, mScaleValue, mScaleValue);
        final AnimatorSet scaleDown_shadow = AnimationHelper.buildScaleDownAnimation(
                imageViewShadow, mScaleValue + shadowAdjustScaleX,
                mScaleValue + shadowAdjustScaleY);
        final AnimatorSet alpha_menu = AnimationHelper.buildAlphaAnimation(scrollViewMenu, 1.0f);

        scaleDown_shadow.addListener(animationListener);
        scaleDown_activity.playTogether(scaleDown_shadow);
        scaleDown_activity.playTogether(alpha_menu);
        scaleDown_activity.start();
    }

    public void closeMenu() {
        isOpened = false;

        final AnimatorSet scaleUp_activity = AnimationHelper.buildScaleUpAnimation(
                viewActivity, 1.0f, 1.0f);
        final AnimatorSet scaleUp_shadow = AnimationHelper.buildScaleUpAnimation(
                imageViewShadow, 1.0f, 1.0f);
        final AnimatorSet alpha_menu = AnimationHelper.buildAlphaAnimation(scrollViewMenu, 0.0f);

        scaleUp_activity.addListener(animationListener);
        scaleUp_activity.playTogether(scaleUp_shadow);
        scaleUp_activity.playTogether(alpha_menu);
        scaleUp_activity.start();
    }

    //==============================================================================================
    // Inner Classes
    //==============================================================================================

    public interface OnMenuListener {
        public void openMenu();

        public void closeMenu();
    }

}
