package com.mopub.mobileads;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.List;

import static com.mopub.mobileads.MoPubErrorCode.UNSPECIFIED;

class HtmlWebViewClient extends WebViewClient {
    private final Context mContext;
    private HtmlWebViewListener mHtmlWebViewListener;
    private BaseHtmlWebView mHtmlWebView;
    private final String mClickthroughUrl;
    private final String mRedirectUrl;

    HtmlWebViewClient(HtmlWebViewListener htmlWebViewListener, BaseHtmlWebView htmlWebView, String clickthrough, String redirect) {
        mHtmlWebViewListener = htmlWebViewListener;
        mHtmlWebView = htmlWebView;
        mClickthroughUrl = clickthrough;
        mRedirectUrl = redirect;
        mContext = htmlWebView.getContext();
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (handleSpecialMoPubScheme(url) || handlePhoneScheme(url)) {
            return true;
        }

        if (isMarketUrl(url) && !canHandleMarketUrl(url)) {
            return true;
        }

        url = urlWithClickTrackingRedirect(url);
        Log.d("MoPub", "Ad clicked. Click URL: " + url);
        mHtmlWebViewListener.onClicked();

        showBrowserForUrl(url);
        return true;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        // If the URL being loaded shares the redirectUrl prefix, open it in the browser.
        if (mRedirectUrl != null && url.startsWith(mRedirectUrl)) {
            url = urlWithClickTrackingRedirect(url);
            view.stopLoading();
            showBrowserForUrl(url);
        }
    }

    private boolean handleSpecialMoPubScheme(String url) {
        if (!url.startsWith("mopub://")) return false;

        Uri uri = Uri.parse(url);
        String host = uri.getHost();

        if (host.equals("finishLoad")) {
            mHtmlWebViewListener.onLoaded(mHtmlWebView);
        } else if (host.equals("close")) {
            mHtmlWebViewListener.onCollapsed();
        } else if (host.equals("failLoad")) {
            mHtmlWebViewListener.onFailed(UNSPECIFIED);
        } else if (host.equals("custom")) {
            handleCustomIntentFromUri(uri);
        }

        return true;
    }

    private boolean handlePhoneScheme(String url) {
        if (!isPhoneIntent(url)) return false;

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            mContext.startActivity(intent);
            mHtmlWebViewListener.onClicked();
        } catch (ActivityNotFoundException e) {
            Log.w("MoPub", "Could not handle intent with URI: " + url +
                    ". Is this intent unsupported on your phone?");
        }

        return true;
    }

    private boolean isPhoneIntent(String url) {
        return url.startsWith("tel:") || url.startsWith("voicemail:") ||
                url.startsWith("sms:") || url.startsWith("mailto:") ||
                url.startsWith("geo:") || url.startsWith("google.streetview:");
    }

    private boolean isMarketUrl(String url) {
        return url.startsWith("market://");
    }

    private boolean canHandleMarketUrl(String url) {
        // Determine which activities can handle the market intent
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        PackageManager packageManager = mContext.getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);

        // If there are no relevant activities, don't follow the link
        boolean isIntentSafe = activities.size() > 0;
        if (!isIntentSafe) {
            Log.w("MoPub", "Could not handle market action: " + url
                    + ". Perhaps you're running in the emulator, which does not have "
                    + "the Android Market?");
            return false;
        }

        return true;
    }

    private String urlWithClickTrackingRedirect(String url) {
        if (mClickthroughUrl == null) {
            return url;
        } else {
            String encodedUrl = Uri.encode(url);
            return mClickthroughUrl + "&r=" + encodedUrl;
        }
    }

    private void showBrowserForUrl(String url) {
        if (url == null || url.equals("")) url = "about:blank";
        Log.d("MoPub", "Final URI to show in browser: " + url);
        Intent intent = new Intent(mContext, MraidBrowser.class);
        intent.putExtra(MraidBrowser.URL_EXTRA, url);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            mContext.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            String action = intent.getAction();
            Log.w("MoPub", "Could not handle intent action: " + action
                    + ". Perhaps you forgot to declare com.mopub.mobileads.MraidBrowser"
                    + " in your Android manifest file.");

            mContext.startActivity(
                    new Intent(Intent.ACTION_VIEW, Uri.parse("about:blank"))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }

    private void handleCustomIntentFromUri(Uri uri) {
        mHtmlWebViewListener.onClicked();
        String action = uri.getQueryParameter("fnc");
        String adData = uri.getQueryParameter("data");
        Intent customIntent = new Intent(action);
        customIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        customIntent.putExtra(HtmlBannerWebView.EXTRA_AD_CLICK_DATA, adData);
        try {
            mContext.startActivity(customIntent);
        } catch (ActivityNotFoundException e) {
            Log.w("MoPub", "Could not handle custom intent: " + action +
                    ". Is your intent spelled correctly?");
        }
    }
}
