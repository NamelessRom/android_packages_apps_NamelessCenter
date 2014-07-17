package org.namelessrom.center.utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AnimationUtils;

import org.namelessrom.center.AppInstance;

/**
 * Helper class for animations
 */
public class AnimationHelper {

    private static final int DEFAULT_DURATION = 250;

    public static AnimatorSet buildScaleUpAnimation(final View target, final float targetScaleX,
            final float targetScaleY) {
        final AnimatorSet scaleUp = new AnimatorSet();
        scaleUp.playTogether(
                ObjectAnimator.ofFloat(target, "scaleX", targetScaleX),
                ObjectAnimator.ofFloat(target, "scaleY", targetScaleY)
        );
        scaleUp.setDuration(DEFAULT_DURATION);
        return scaleUp;
    }

    public static AnimatorSet buildAlphaAnimation(final View target, final float alpha) {
        final AnimatorSet alphaAnimation = new AnimatorSet();
        alphaAnimation.playTogether(ObjectAnimator.ofFloat(target, "alpha", alpha));
        alphaAnimation.setDuration(DEFAULT_DURATION);
        return alphaAnimation;
    }

    public static AnimatorSet buildScaleDownAnimation(final View target, final float targetScaleX,
            final float targetScaleY) {
        final AnimatorSet scaleDown = new AnimatorSet();
        scaleDown.playTogether(
                ObjectAnimator.ofFloat(target, "scaleX", targetScaleX),
                ObjectAnimator.ofFloat(target, "scaleY", targetScaleY)
        );
        scaleDown.setInterpolator(AnimationUtils.loadInterpolator(AppInstance.applicationContext,
                android.R.anim.decelerate_interpolator));
        scaleDown.setDuration(DEFAULT_DURATION);
        return scaleDown;
    }

}
