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

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AnimationUtils;

import org.namelessrom.center.AppInstance;

/**
 * Helper class for animations
 */
public class AnimationHelper {

    public static final int DEFAULT_DURATION = 250;

    public static ObjectAnimator alpha(final View v, final float from, final float to) {
        return alpha(v, from, to, DEFAULT_DURATION);
    }

    public static ObjectAnimator alpha(final View v, final float from, final float to,
            final int duration) {
        final ObjectAnimator animator = ObjectAnimator.ofFloat(v, "alpha", from, to);
        animator.setDuration(duration);
        return animator;
    }

    public static ObjectAnimator scaleX(final View v, final float from, final float to) {
        return scaleX(v, from, to, DEFAULT_DURATION);
    }

    public static ObjectAnimator scaleX(final View v, final float from, final float to,
            final int duration) {
        final ObjectAnimator animator = ObjectAnimator.ofFloat(v, "scaleX", from, to);
        animator.setDuration(duration);
        return animator;
    }

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
