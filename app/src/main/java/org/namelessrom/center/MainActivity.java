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

package org.namelessrom.center;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.special.ResideMenu.ResideMenu;
import com.special.ResideMenu.ResideMenuItem;

import org.namelessrom.center.utils.AnimationHelper;
import org.namelessrom.center.utils.DrawableHelper;

import java.util.ArrayList;

public class MainActivity extends Activity implements View.OnClickListener {

    private ResideMenu mResideMenu;

    private static final Object[] MENU_LEFT_ICONS = new Object[]{
            DrawableHelper.getSvgDrawable(R.raw.svg_home),
            DrawableHelper.getSvgDrawable(R.raw.svg_updates),
            DrawableHelper.getSvgDrawable(R.raw.svg_preferences),
    };

    private static final int[] MENU_LEFT_TITLES = new int[]{
            R.string.home,
            R.string.updates,
            R.string.preferences
    };

    private static final int[] MENU_LEFT_IDS = new int[]{
            Constants.MENU_ID_HOME,
            Constants.MENU_ID_UPDATES,
            Constants.MENU_ID_PREFERENCES
    };

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupActionBar();

        mResideMenu = setupMenu();

        loadFragment(new PlaceholderFragment());
    }

    private void setupActionBar() {
        final ActionBar actionBar = getActionBar();
        if (actionBar == null) return;

        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private ResideMenu setupMenu() {
        final ResideMenu resideMenu = new ResideMenu(this);

        resideMenu.setBackground(R.drawable.main_background);
        resideMenu.attachToActivity(this);
        resideMenu.setScaleValue(0.6f);

        // Disable the right menu
        resideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_RIGHT);

        resideMenu.setMenuItems(buildMenuLeft(), ResideMenu.DIRECTION_LEFT);

        return resideMenu;
    }

    private ArrayList<ResideMenuItem> buildMenuLeft() {
        final ArrayList<ResideMenuItem> menuItems = new ArrayList<ResideMenuItem>(1);

        ResideMenuItem item;
        for (int i = 0; i < MENU_LEFT_ICONS.length; i++) {
            item = new ResideMenuItem(this, MENU_LEFT_ICONS[i], MENU_LEFT_TITLES[i]);
            item.setMenuId(MENU_LEFT_IDS[i]);
            item.setOnClickListener(this);
            menuItems.add(item);
        }

        return menuItems;
    }

    @Override public void onClick(final View v) {
        if (!(v instanceof ResideMenuItem)) return;

        Fragment fragment = null;
        final int id = ((ResideMenuItem) v).getMenuId();

        final ObjectAnimator animator =
                AnimationHelper.scaleX(((ResideMenuItem) v).getIcon(), 0.0f, 1.0f, 200);
        animator.addListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator animation) { }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (id == Constants.MENU_ID_PREFERENCES) {
                    final Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(i);
                    overridePendingTransition(R.anim.enter_left, R.anim.enter_right);
                }
            }

            @Override public void onAnimationCancel(Animator animation) { }

            @Override public void onAnimationRepeat(Animator animation) { }
        });
        animator.start();

        switch (id) {
            default:
            case Constants.MENU_ID_HOME:
            case Constants.MENU_ID_UPDATES:
                fragment = new PlaceholderFragment();
                break;
            case Constants.MENU_ID_PREFERENCES:
                // handled by animation listener
                break;
        }
        if (fragment != null) loadFragment(fragment);
    }

    @Override public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();

        switch (id) {
            default:
            case android.R.id.home:
                if (!mResideMenu.isOpened()) mResideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override public void onBackPressed() {
        if (mResideMenu != null && mResideMenu.isOpened()) {
            mResideMenu.closeMenu();
        } else if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override public boolean dispatchTouchEvent(@NonNull MotionEvent ev) {
        return mResideMenu.dispatchTouchEvent(ev);
    }

    private void loadFragment(final Fragment fragment) {
        mResideMenu.clearIgnoredViewList();
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
        if (mResideMenu.isOpened()) mResideMenu.closeMenu();
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() { }

        @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //mResideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
                }
            });
            return rootView;
        }
    }
}
