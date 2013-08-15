package com.mopub.mobileads.util;

import android.webkit.WebView;

import java.lang.reflect.Method;

public class WebViews {
    public static void onPause(WebView webView) {
        try {
            Method onPause = WebView.class.getDeclaredMethod("onPause");
            onPause.invoke(webView);
        } catch (Exception e) {
            // can't call this before API level 11
            return;
        }
    }

    public static void onResume(WebView webView) {
        try {
            Method onResume = WebView.class.getDeclaredMethod("onResume");
            onResume.invoke(webView);
        } catch (Exception e) {
            // can't call this before API level 11
            return;
        }
    }
}
