package com.mopub.mobileads;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.util.Map;

import static com.mopub.mobileads.AdFetcher.MRAID_HTML_DATA;
import static com.mopub.mobileads.BaseActivity.SOURCE_KEY;
import static com.mopub.mobileads.MoPubErrorCode.MRAID_LOAD_ERROR;

class MraidInterstitial extends CustomEventInterstitial {
    private Activity mActivity;
    private String mHtmlData;

    @Override
    protected void loadInterstitial(Context context,
                          CustomEventInterstitialListener customEventInterstitialListener,
                          Map<String, Object> localExtras,
                          Map<String, String> serverExtras) {

        if (context instanceof Activity) {
            mActivity = (Activity) context;
        } else {
            customEventInterstitialListener.onInterstitialFailed(MRAID_LOAD_ERROR);
            return;
        }

        if (extrasAreValid(serverExtras)) {
            mHtmlData = Uri.decode(serverExtras.get(MRAID_HTML_DATA));
        } else {
            customEventInterstitialListener.onInterstitialFailed(MRAID_LOAD_ERROR);
            return;
        }

        customEventInterstitialListener.onInterstitialLoaded();
    }

    @Override
    protected void showInterstitial() {
        Intent intent = new Intent(mActivity, MraidActivity.class);
        intent.putExtra(SOURCE_KEY, mHtmlData);
        mActivity.startActivity(intent);
    }

    @Override
    protected void onInvalidate() {
    }

    private boolean extrasAreValid(Map<String,String> serverExtras) {
        return serverExtras.containsKey(MRAID_HTML_DATA);
    }
}
