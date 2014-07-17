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

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.namelessrom.center.R;
import org.namelessrom.center.widgets.CircleImageView;

import static butterknife.ButterKnife.findById;

public class ResideMenuItem extends LinearLayout {

    private CircleImageView icon;
    private TextView        title;

    private int menuId;

    public ResideMenuItem(final Context context) {
        super(context);
        initViews(context);
    }

    public ResideMenuItem(final Context context, final Object icon, final int title) {
        super(context);
        initViews(context);
        if (icon instanceof Integer) {
            this.icon.setImageResource((Integer) icon);
        } else if (icon instanceof Drawable) {
            this.icon.setImageDrawable((Drawable) icon);
        }
        this.title.setText(title);
    }

    private void initViews(final Context context) {
        View.inflate(context, R.layout.residemenu_item, this);
        icon = findById(this, R.id.iv_icon);
        title = findById(this, R.id.tv_title);
    }

    public CircleImageView getIcon() { return this.icon; }

    public TextView getTitle() { return this.title; }

    public void setIcon(final int icon) { this.icon.setImageResource(icon); }

    public void setTitle(final int title) { this.title.setText(title); }

    public void setTitle(final String title) { this.title.setText(title); }

    public int getMenuId() { return menuId; }

    public void setMenuId(final int menuId) { this.menuId = menuId; }
}
