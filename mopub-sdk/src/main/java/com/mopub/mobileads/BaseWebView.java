package com.mopub.mobileads;

import android.content.Context;
import android.webkit.WebView;

public class BaseWebView extends WebView {
    public BaseWebView(Context context) {
        /*
         * Important: don't allow any WebView subclass to be instantiated using
         * an Activity context, as it will leak on Froyo devices and earlier.
         */
        super(context.getApplicationContext());
    }
}
