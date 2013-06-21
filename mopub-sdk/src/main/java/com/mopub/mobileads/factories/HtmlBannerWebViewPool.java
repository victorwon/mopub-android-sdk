package com.mopub.mobileads.factories;

import android.content.Context;
import com.mopub.mobileads.HtmlBannerWebView;

import static com.mopub.mobileads.CustomEventBanner.CustomEventBannerListener;

public class HtmlBannerWebViewPool extends BaseHtmlWebViewPool<HtmlBannerWebView, CustomEventBannerListener> {

    HtmlBannerWebViewPool(Context context) {
        super(context);
    }

    @Override
    protected HtmlBannerWebView createNewHtmlWebView() {
        return new HtmlBannerWebView(mContext);
    }

    @Override
    protected void initializeHtmlWebView(
            HtmlBannerWebView htmlWebView, CustomEventBannerListener customEventListener,
            boolean isScrollable,
            String redirectUrl,
            String clickthroughUrl) {
        htmlWebView.init(customEventListener, isScrollable, redirectUrl, clickthroughUrl);
    }
}
