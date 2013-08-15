package com.mopub.mobileads.test.support;

import com.mopub.mobileads.HtmlInterstitialWebView;
import com.mopub.mobileads.factories.HtmlInterstitialWebViewFactory;
import com.mopub.mobileads.factories.HtmlInterstitialWebViewPool;

import static com.mopub.mobileads.CustomEventInterstitial.CustomEventInterstitialListener;
import static org.mockito.Mockito.mock;

public class TestHtmlInterstitialWebViewFactory extends HtmlInterstitialWebViewFactory {
    private HtmlInterstitialWebView mockHtmlInterstitialWebView = mock(HtmlInterstitialWebView.class);

    private CustomEventInterstitialListener mLatestListener;
    private boolean mLatestIsScrollable;
    private String mLatestRedirectUrl;
    private String mLatestClickthroughUrl;


    @Override
    public HtmlInterstitialWebView internalCreate(CustomEventInterstitialListener customEventInterstitialListener, boolean isScrollable, String redirectUrl, String clickthroughUrl) {
        mLatestListener = customEventInterstitialListener;
        mLatestIsScrollable = isScrollable;
        mLatestRedirectUrl = redirectUrl;
        mLatestClickthroughUrl = clickthroughUrl;
        return getInstance().mockHtmlInterstitialWebView;
    }

    private static TestHtmlInterstitialWebViewFactory getInstance() {
        return (TestHtmlInterstitialWebViewFactory) instance;
    }

    public static HtmlInterstitialWebView getSingletonMock() {
        return getInstance().mockHtmlInterstitialWebView;
    }

    public static CustomEventInterstitialListener getLatestListener() {
        return getInstance().mLatestListener;
    }

    public static boolean getLatestIsScrollable() {
        return getInstance().mLatestIsScrollable;
    }
    public static String getLatestRedirectUrl() {
        return getInstance().mLatestRedirectUrl;
    }

    public static String getLatestClickthroughUrl() {
        return getInstance().mLatestClickthroughUrl;
    }

    public static HtmlInterstitialWebViewPool getWebViewPool() {
        return getInstance().mHtmlInterstitialWebViewPool;
    }
}
