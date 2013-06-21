package com.mopub.mobileads.factories;

import android.content.Context;
import com.mopub.mobileads.HtmlInterstitialWebView;

import static com.mopub.mobileads.CustomEventInterstitial.CustomEventInterstitialListener;

public class HtmlInterstitialWebViewFactory {
    protected static HtmlInterstitialWebViewFactory instance = new HtmlInterstitialWebViewFactory();
    protected HtmlInterstitialWebViewPool mHtmlInterstitialWebViewPool;
    private int mRefCount;

    public static void initialize(Context context) {
        instance.initializeInstance(context);
    }

    public static void cleanup() {
        instance.cleanupInstance();
    }

    private void initializeInstance(Context context) {
        if (mHtmlInterstitialWebViewPool == null) {
            mHtmlInterstitialWebViewPool = new HtmlInterstitialWebViewPool(context);
        }
        mRefCount++;
    }

    private void cleanupInstance() {
        if (--mRefCount == 0) {
            mHtmlInterstitialWebViewPool.cleanup();
            mHtmlInterstitialWebViewPool = null;
        }
    }

    @Deprecated // for testing
    public static void setInstance(HtmlInterstitialWebViewFactory factory) {
        instance = factory;
    }

    public static HtmlInterstitialWebView create(
            CustomEventInterstitialListener customEventInterstitialListener,
            boolean isScrollable,
            String redirectUrl,
            String clickthroughUrl) {
        return instance.internalCreate(customEventInterstitialListener, isScrollable, redirectUrl, clickthroughUrl);
    }

    public HtmlInterstitialWebView internalCreate(
            CustomEventInterstitialListener customEventInterstitialListener,
            boolean isScrollable,
            String redirectUrl,
            String clickthroughUrl) {
        return instance.mHtmlInterstitialWebViewPool.getNextHtmlWebView(
                customEventInterstitialListener,
                isScrollable,
                redirectUrl,
                clickthroughUrl);
    }
}
