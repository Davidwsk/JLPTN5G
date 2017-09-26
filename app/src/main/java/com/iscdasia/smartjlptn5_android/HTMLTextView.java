package com.iscdasia.smartjlptn5_android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.Spanned;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by iscd-dev01 on 26/9/2017.
 */

public class HTMLTextView extends android.support.v7.widget.AppCompatTextView {

    public HTMLTextView(Context context) {
        super(context);
        init();
    }

    public HTMLTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HTMLTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        setText(fromHtml(getText().toString()));
    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String source) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(source);
        }
    }
}

