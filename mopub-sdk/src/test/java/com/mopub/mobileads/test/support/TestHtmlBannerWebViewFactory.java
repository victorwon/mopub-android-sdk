package com.mopub.mobileads.test.support;

import android.webkit.WebSettings;
import com.mopub.mobileads.HtmlBannerWebView;
import com.mopub.mobileads.factories.HtmlBannerWebViewFactory;
import com.mopub.mobileads.factories.HtmlBannerWebViewPool;

import static com.mopub.mobileads.CustomEventBanner.CustomEventBannerListener;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

public class TestHtmlBannerWebViewFactory extends HtmlBannerWebViewFactory {
    private HtmlBannerWebView mockHtmlBannerWebView = mock(HtmlBannerWebView.class);
    private CustomEventBannerListener mLatestListener;
    private boolean mLatestIsScrollable;
    private String mLatestRedirectUrl;
    private String mLatestClickthroughUrl;

    public TestHtmlBannerWebViewFactory() {
        WebSettings webSettings = mock(WebSettings.class);
        stub(mockHtmlBannerWebView.getSettings()).toReturn(webSettings);
        stub(webSettings.getUserAgentString()).toReturn("Mozilla/5.0 (Linux; U; Android 4.0.3; ko-kr; LG-L160L Build/IML74K) AppleWebkit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
    }

    public static HtmlBannerWebView getSingletonMock() {
        return getInstance().mockHtmlBannerWebView;
    }

    public static CustomEventBannerListener getLatestListener() {
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

    private static TestHtmlBannerWebViewFactory getInstance() {
        return (TestHtmlBannerWebViewFactory) instance;
    }

    @Override
    public HtmlBannerWebView internalCreate(CustomEventBannerListener customEventBannerListener, boolean isScrollable, String redirectUrl, String clickthroughUrl) {
        mLatestListener = customEventBannerListener;
        mLatestIsScrollable = isScrollable;
        mLatestRedirectUrl = redirectUrl;
        mLatestClickthroughUrl = clickthroughUrl;
        return getSingletonMock();
    }

    public static HtmlBannerWebViewPool getWebViewPool() {
        return getInstance().mHtmlBannerWebViewPool;
    }
}
