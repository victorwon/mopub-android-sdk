package com.mopub.mobileads;

import android.content.Context;
import android.os.Handler;

import static com.mopub.mobileads.CustomEventInterstitial.CustomEventInterstitialListener;

public class HtmlInterstitialWebView extends BaseHtmlWebView {
    private Handler mHandler;

    public HtmlInterstitialWebView(Context context) {
        super(context);

        mHandler = new Handler();
    }

    public void init(CustomEventInterstitialListener customEventInterstitialListener, boolean isScrollable, String redirectUrl, String clickthroughUrl) {
        super.init(isScrollable);
        setWebViewClient(new HtmlWebViewClient(new HtmlInterstitialWebViewListener(customEventInterstitialListener), this, clickthroughUrl, redirectUrl));
        addMoPubUriJavascriptInterface(customEventInterstitialListener);
    }

    private void postHandlerRunnable(Runnable r) {
        mHandler.post(r);
    }

    /*
     * XXX (2/15/12): This is a workaround for a problem on ICS devices where
     * WebViews with layout height WRAP_CONTENT can mysteriously render with
     * zero height. This seems to happen when calling loadData() with HTML that
     * sets window.location during its "onload" event. We use loadData() when
     * displaying interstitials, and our creatives use window.location to
     * communicate ad loading status to AdViews. This results in zero-height
     * interstitials. We counteract this by using a Javascript interface object
     * to signal loading status, rather than modifying window.location.
     */
    private void addMoPubUriJavascriptInterface(final CustomEventInterstitialListener customEventInterstitialListener) {
        final class MoPubUriJavascriptInterface {
            // This method appears to be unused, since it will only be called from JavaScript.
            @SuppressWarnings("unused")
            public boolean fireFinishLoad() {
                HtmlInterstitialWebView.this.postHandlerRunnable(new Runnable() {
                    @Override
                    public void run() {
                        customEventInterstitialListener.onInterstitialShown();
                    }
                });
                return true;
            }
        }

        addJavascriptInterface(new MoPubUriJavascriptInterface(), "mopubUriInterface");
    }

    static class HtmlInterstitialWebViewListener implements HtmlWebViewListener {
        private final CustomEventInterstitialListener mCustomEventInterstitialListener;

        public HtmlInterstitialWebViewListener(CustomEventInterstitialListener customEventInterstitialListener) {
            mCustomEventInterstitialListener = customEventInterstitialListener;
        }

        @Override
        public void onLoaded(BaseHtmlWebView mHtmlWebView) {
            // When the HtmlInterstitialWebViewClient loads, it means that our HtmlInterstitial has been shown.
            mCustomEventInterstitialListener.onInterstitialShown();
        }

        @Override
        public void onFailed(MoPubErrorCode errorCode) {
            mCustomEventInterstitialListener.onInterstitialFailed(errorCode);
        }

        @Override
        public void onClicked() {
            mCustomEventInterstitialListener.onInterstitialClicked();
        }

        @Override
        public void onCollapsed() {
            // Ignored
        }
    }
}
