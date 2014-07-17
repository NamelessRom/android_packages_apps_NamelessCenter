package org.namelessrom.center;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.special.ResideMenu.Constants;
import com.special.ResideMenu.ResideMenu;
import com.special.ResideMenu.ResideMenuItem;

import java.util.ArrayList;

public class MainActivity extends Activity implements View.OnClickListener {

    private ResideMenu mResideMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        resideMenu.setMenuListener(mMenuListener);
        resideMenu.setScaleValue(0.6f);

        // Do not let the user swipe left and right, we open it via the action bar + gestures
        resideMenu.setUseGestures(true);

        // Disable the right menu
        resideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_RIGHT);

        resideMenu.setMenuItems(buildMenuLeft(), ResideMenu.DIRECTION_LEFT);

        return resideMenu;
    }

    private ArrayList<ResideMenuItem> buildMenuLeft() {
        final ArrayList<ResideMenuItem> menuItems = new ArrayList<ResideMenuItem>(1);

        final ResideMenuItem menuHome = new ResideMenuItem(this, R.drawable.ic_launcher,
                R.string.home);
        menuHome.setMenuId(Constants.MENU_ID_HOME);
        menuHome.setOnClickListener(this);
        menuItems.add(menuHome);

        return menuItems;
    }

    @Override
    public void onClick(final View v) {
        if (!(v instanceof ResideMenuItem)) return;

        final Fragment fragment;
        final int id = ((ResideMenuItem) v).getMenuId();
        switch (id) {
            default:
            case Constants.MENU_ID_HOME:
                fragment = new PlaceholderFragment();
                break;
        }

        loadFragment(fragment);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();

        switch (id) {
            default:
            case android.R.id.home:
                if (!mResideMenu.isOpened()) mResideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent ev) {
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

    private final ResideMenu.OnMenuListener mMenuListener = new ResideMenu.OnMenuListener() {
        @Override
        public void openMenu() {

        }

        @Override
        public void closeMenu() {

        }
    };

    /**
     * A placeholder fragment containing a simple view.
     */
    public class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
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
