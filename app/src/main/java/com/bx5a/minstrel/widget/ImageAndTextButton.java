/*
 * Copyright Guillaume VINCKE 2016
 *
 * This file is part of Minstrel
 *
 * Minstrel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Minstrel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Minstrel.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.bx5a.minstrel.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bx5a.minstrel.R;

/**
 * A button that displays in a horizontal layout first an image and then a button
 */
public class ImageAndTextButton extends LinearLayout {
    private LinearLayout button;
    private TextView textView;

    public ImageAndTextButton(Context context) {
        super(context);
    }

    public ImageAndTextButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ImageAndTextButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public void init(Context context, AttributeSet attrs) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.image_and_text_button, this);

        button = (LinearLayout) findViewById(R.id.imageAndTextButton_button);
        ImageView image = (ImageView) findViewById(R.id.imageAndTextButton_image);
        textView = (TextView) findViewById(R.id.imageAndTextButton_text);

        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.ImageAndTextButton);

        String textValue = attributes.getString(R.styleable.ImageAndTextButton_text);
        textView.setText(textValue);

        Drawable srcValue = attributes.getDrawable(R.styleable.ImageAndTextButton_src);
        image.setImageDrawable(srcValue);

        if (!attributes.hasValue(R.styleable.ImageAndTextButton_textColor)) {
            return;
        }
        ColorStateList textColor = attributes.getColorStateList(R.styleable.ImageAndTextButton_textColor);
        textView.setTextColor(textColor);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        super.setOnClickListener(l);
        button.setOnClickListener(l);
    }

    public void setText(String text) {
        textView.setText(text);
    }
}
