package com.bx5a.minstrel.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bx5a.minstrel.R;

/**
 * Created by guillaume on 21/04/2016.
 */
public class ImageAndTextButton extends LinearLayout {
    private LinearLayout button;

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
        TextView text = (TextView) findViewById(R.id.imageAndTextButton_text);

        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.ImageAndTextButton);
        String textValue = attributes.getString(R.styleable.ImageAndTextButton_text);
        Drawable srcValue = attributes.getDrawable(R.styleable.ImageAndTextButton_src);

        image.setImageDrawable(srcValue);
        text.setText(textValue);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        super.setOnClickListener(l);
        button.setOnClickListener(l);
    }
}
