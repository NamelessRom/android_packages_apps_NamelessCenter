package com.special.ResideMenu;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.namelessrom.center.R;

/**
 * User: special
 * Date: 13-12-10
 * Time: 下午11:05
 * Mail: specialcyci@gmail.com
 */
public class ResideMenuItem extends LinearLayout {

    private ImageView icon;
    private TextView  title;

    private int menuId;

    public ResideMenuItem(final Context context) {
        super(context);
        initViews(context);
    }

    public ResideMenuItem(final Context context, final int icon, final int title) {
        super(context);
        initViews(context);

        this.icon.setImageResource(icon);
        this.title.setText(title);
    }

    public ResideMenuItem(final Context context, final int icon, final String title) {
        super(context);
        initViews(context);
        this.icon.setImageResource(icon);
        this.title.setText(title);
    }

    private void initViews(final Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.residemenu_item, this);
        icon = (ImageView) findViewById(R.id.iv_icon);
        title = (TextView) findViewById(R.id.tv_title);
    }

    public void setIcon(final int icon) { this.icon.setImageResource(icon); }

    public void setTitle(final int title) { this.title.setText(title); }

    public void setTitle(final String title) { this.title.setText(title); }

    public int getMenuId() { return menuId; }

    public void setMenuId(final int menuId) { this.menuId = menuId; }
}
