package com.tencent.zebra.effect;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class RotateBgImageView extends ImageView {

    public RotateBgImageView(Context context) {
        super(context);
    }

    public RotateBgImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void manulSetFrame(int l, int t, int r, int b) {
        setFrame(l, t, r, b);
    }

}
