package com.mopub.mobileads.factories;

import android.content.Context;
import com.mopub.mobileads.HtmlBannerWebView;

import static com.mopub.mobileads.CustomEventBanner.CustomEventBannerListener;

public class HtmlBannerWebViewFactory {
    protected static HtmlBannerWebViewFactory instance = new HtmlBannerWebViewFactory();
    protected HtmlBannerWebViewPool mHtmlBannerWebViewPool;
    private int mRefCount;

    public static HtmlBannerWebView create(CustomEventBannerListener customEventBannerListener, boolean isScrollable, String redirectUrl, String clickthroughUrl) {
        return instance.internalCreate(customEventBannerListener, isScrollable, redirectUrl, clickthroughUrl);
    }

    public static void initialize(Context context) {
        instance.initializeInstance(context);
    }

    public static void cleanup() {
        instance.cleanupInstance();
    }

    private void initializeInstance(Context context) {
        if (mHtmlBannerWebViewPool == null) {
            mHtmlBannerWebViewPool = new HtmlBannerWebViewPool(context);
        }
        mRefCount++;
    }

    private void cleanupInstance() {
        if (--mRefCount == 0) {
            mHtmlBannerWebViewPool.cleanup();
            mHtmlBannerWebViewPool = null;
        }
    }

    public HtmlBannerWebView internalCreate(CustomEventBannerListener customEventBannerListener, boolean isScrollable, String redirectUrl, String clickthroughUrl) {
        return instance.mHtmlBannerWebViewPool.getNextHtmlWebView(customEventBannerListener, isScrollable, redirectUrl, clickthroughUrl);
    }

    @Deprecated // for testing
    public static void setInstance(HtmlBannerWebViewFactory factory) {
        instance = factory;
    }
}

