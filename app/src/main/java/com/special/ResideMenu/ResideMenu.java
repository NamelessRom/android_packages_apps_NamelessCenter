package com.special.ResideMenu;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import org.namelessrom.center.R;
import org.namelessrom.center.utils.ViewHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * User: special
 * Date: 13-12-10
 * Time: 下午10:44
 * Mail: specialcyci@gmail.com
 */
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

    private List<View>           ignoredViews;
    private List<ResideMenuItem> leftMenuItems;
    private List<ResideMenuItem> rightMenuItems;

    private OnMenuListener menuListener;
    private float          lastRawX;
    private boolean isInIgnoredView = false;
    private int     scaleDirection  = DIRECTION_LEFT;
    private int     pressedState    = PRESSED_DOWN;
    private float   mScaleValue     = 0.5f;

    public ResideMenu(final Context context) {
        super(context);
        initViews(context);
    }

    private void initViews(final Context context) {
        final LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.residemenu, this);

        scrollViewLeftMenu = (ScrollView) findViewById(R.id.sv_left_menu);
        scrollViewRightMenu = (ScrollView) findViewById(R.id.sv_right_menu);
        imageViewShadow = (ImageView) findViewById(R.id.iv_shadow);
        layoutLeftMenu = (LinearLayout) findViewById(R.id.layout_left_menu);
        layoutRightMenu = (LinearLayout) findViewById(R.id.layout_right_menu);
        imageViewBackground = (ImageView) findViewById(R.id.iv_background);
    }

    /**
     * use the method to set up the activity which residemenu need to show;
     *
     * @param activity
     */
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

    /**
     * set the menu background picture;
     *
     * @param imageResrouce
     */
    public void setBackground(final int imageResrouce) {
        imageViewBackground.setImageResource(imageResrouce);
    }

    /**
     * the visiblity of shadow under the activity view;
     *
     * @param isVisible
     */
    public void setShadowVisible(final boolean isVisible) {
        if (isVisible) {
            imageViewShadow.setImageResource(R.drawable.shadow);
        } else {
            imageViewShadow.setImageBitmap(null);
        }
    }

    /**
     * add a single items;
     *
     * @param menuItem
     * @param direction
     */
    public void addMenuItem(final ResideMenuItem menuItem, final int direction) {
        if (direction == DIRECTION_LEFT) {
            this.leftMenuItems.add(menuItem);
            layoutLeftMenu.addView(menuItem);
        } else {
            this.rightMenuItems.add(menuItem);
            layoutRightMenu.addView(menuItem);
        }
    }

    /**
     * set the menu items by array list;
     *
     * @param menuItems
     * @param direction
     */
    public void setMenuItems(final List<ResideMenuItem> menuItems, final int direction) {
        if (direction == DIRECTION_LEFT) {
            this.leftMenuItems = menuItems;
        } else {
            this.rightMenuItems = menuItems;
        }
        rebuildMenu();
    }

    private void rebuildMenu() {
        layoutLeftMenu.removeAllViews();
        layoutRightMenu.removeAllViews();
        for (final ResideMenuItem item : leftMenuItems) layoutLeftMenu.addView(item);
        for (final ResideMenuItem item : rightMenuItems) layoutRightMenu.addView(item);
    }

    /**
     * get the menu items;
     *
     * @return
     */
    public List<ResideMenuItem> getMenuItems(final int direction) {
        if (direction == DIRECTION_LEFT) { return leftMenuItems; } else { return rightMenuItems; }
    }

    /**
     * if you need to do something on the action of closing or opening
     * menu, set the listener here.
     *
     * @return
     */
    public void setMenuListener(final OnMenuListener menuListener) {
        this.menuListener = menuListener;
    }


    public OnMenuListener getMenuListener() { return menuListener; }

    /**
     * we need the call the method before the menu show, because the
     * padding of activity can't get at the moment of onCreateView();
     */
    private void setViewPadding() {
        this.setPadding(viewActivity.getPaddingLeft(),
                viewActivity.getPaddingTop(),
                viewActivity.getPaddingRight(),
                viewActivity.getPaddingBottom());
    }

    /**
     * show the reside menu;
     */
    public void openMenu(final int direction) {
        setScaleDirection(direction);
        isOpened = true;

        final AnimatorSet scaleDown_activity = buildScaleDownAnimation(viewActivity, mScaleValue,
                mScaleValue);
        final AnimatorSet scaleDown_shadow = buildScaleDownAnimation(imageViewShadow,
                mScaleValue + shadowAdjustScaleX, mScaleValue + shadowAdjustScaleY);
        final AnimatorSet alpha_menu = buildMenuAnimation(scrollViewMenu, 1.0f);

        scaleDown_shadow.addListener(animationListener);
        scaleDown_activity.playTogether(scaleDown_shadow);
        scaleDown_activity.playTogether(alpha_menu);
        scaleDown_activity.start();
    }

    /**
     * close the reslide menu;
     */
    public void closeMenu() {
        isOpened = false;

        final AnimatorSet scaleUp_activity = buildScaleUpAnimation(viewActivity, 1.0f, 1.0f);
        final AnimatorSet scaleUp_shadow = buildScaleUpAnimation(imageViewShadow, 1.0f, 1.0f);
        final AnimatorSet alpha_menu = buildMenuAnimation(scrollViewMenu, 0.0f);

        scaleUp_activity.addListener(animationListener);
        scaleUp_activity.playTogether(scaleUp_shadow);
        scaleUp_activity.playTogether(alpha_menu);
        scaleUp_activity.start();
    }

    public void setSwipeDirectionDisable(final int direction) {
        disabledSwipeDirection.add(direction);
    }

    private boolean isInDisableDirection(final int direction) {
        return disabledSwipeDirection.contains(direction);
    }

    private void setScaleDirection(int direction) {
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

    /**
     * return the flag of menu status;
     *
     * @return
     */
    public boolean isOpened() { return isOpened; }

    private OnClickListener viewActivityOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if (isOpened()) closeMenu();
        }
    };

    private Animator.AnimatorListener animationListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {
            if (isOpened()) {
                scrollViewMenu.setVisibility(VISIBLE);
                if (menuListener != null) { menuListener.openMenu(); }
            } else {
                manageLayers(true);
            }
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            // reset the view;
            if (isOpened()) {
                viewActivity.setTouchDisable(true);
                viewActivity.setOnClickListener(viewActivityOnClickListener);
            } else {
                viewActivity.setTouchDisable(false);
                viewActivity.setOnClickListener(null);
                scrollViewMenu.setVisibility(GONE);
                if (menuListener != null) { menuListener.closeMenu(); }
                manageLayers(false);
            }
        }

        @Override
        public void onAnimationCancel(Animator animation) { }

        @Override
        public void onAnimationRepeat(Animator animation) { }
    };

    private void manageLayers(final boolean animationStart) {
        if (getHandler() == null) return;
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                final int layerType = (animationStart ? View.LAYER_TYPE_HARDWARE : View.LAYER_TYPE_NONE);
                scrollViewMenu.setLayerType(layerType, null);
                viewActivity.setLayerType(layerType, null);
            }
        });
    }

    /**
     * a helper method to build scale down animation;
     *
     * @param target
     * @param targetScaleX
     * @param targetScaleY
     * @return
     */
    private AnimatorSet buildScaleDownAnimation(final View target, final float targetScaleX,
            final float targetScaleY) {
        final AnimatorSet scaleDown = new AnimatorSet();
        scaleDown.playTogether(
                ObjectAnimator.ofFloat(target, "scaleX", targetScaleX),
                ObjectAnimator.ofFloat(target, "scaleY", targetScaleY)
        );
        scaleDown.setInterpolator(AnimationUtils.loadInterpolator(activity,
                android.R.anim.decelerate_interpolator));
        scaleDown.setDuration(250);
        return scaleDown;
    }

    /**
     * a helper method to build scale up animation;
     *
     * @param target
     * @param targetScaleX
     * @param targetScaleY
     * @return
     */
    private AnimatorSet buildScaleUpAnimation(final View target, final float targetScaleX,
            final float targetScaleY) {
        final AnimatorSet scaleUp = new AnimatorSet();
        scaleUp.playTogether(
                ObjectAnimator.ofFloat(target, "scaleX", targetScaleX),
                ObjectAnimator.ofFloat(target, "scaleY", targetScaleY)
        );
        scaleUp.setDuration(250);
        return scaleUp;
    }

    private AnimatorSet buildMenuAnimation(final View target, final float alpha) {
        final AnimatorSet alphaAnimation = new AnimatorSet();
        alphaAnimation.playTogether(
                ObjectAnimator.ofFloat(target, "alpha", alpha)
        );
        alphaAnimation.setDuration(250);
        return alphaAnimation;
    }

    /**
     * if there ware some view you don't want reside menu
     * to intercept their touch event,you can use the method
     * to set.
     *
     * @param v
     */
    public void addIgnoredView(View v) { ignoredViews.add(v); }

    /**
     * remove the view from ignored view list;
     *
     * @param v
     */
    public void removeIgnoredView(View v) { ignoredViews.remove(v); }

    /**
     * clear the ignored view list;
     */
    public void clearIgnoredViewList() { ignoredViews.clear(); }

    /**
     * if the motion evnent was relative to the view
     * which in ignored view list,return true;
     *
     * @param ev
     * @return
     */
    private boolean isInIgnoredView(final MotionEvent ev) {
        final Rect rect = new Rect();
        for (final View v : ignoredViews) {
            v.getGlobalVisibleRect(rect);
            if (rect.contains((int) ev.getX(), (int) ev.getY())) return true;
        }
        return false;
    }

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

    private float lastActionDownX, lastActionDownY;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
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

    public int getScreenHeight() { return displayMetrics.heightPixels; }

    public int getScreenWidth() { return displayMetrics.widthPixels; }

    public void setScaleValue(final float scaleValue) { this.mScaleValue = scaleValue; }

    public interface OnMenuListener {
        public void openMenu();

        public void closeMenu();
    }

}
