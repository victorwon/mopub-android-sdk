package com.mopub.mobileads;

import android.content.Context;

import java.util.Map;

import static com.mopub.mobileads.AdFetcher.HTML_RESPONSE_BODY_KEY;
import static com.mopub.mobileads.MoPubErrorCode.NETWORK_INVALID_STATE;

abstract class ResponseBodyInterstitial extends CustomEventInterstitial {
    private EventForwardingBroadcastReceiver mBroadcastReceiver;
    protected Context mContext;

    abstract protected void extractExtras(Map<String, String> serverExtras);
    abstract protected void showInterstitial();

    @Override
    protected void loadInterstitial(
            Context context,
            CustomEventInterstitialListener customEventInterstitialListener,
            Map<String, Object> localExtras,
            Map<String, String> serverExtras) {

        mContext = context;

        if (extrasAreValid(serverExtras)) {
            extractExtras(serverExtras);
        } else {
            customEventInterstitialListener.onInterstitialFailed(NETWORK_INVALID_STATE);
            return;
        }

        mBroadcastReceiver = new EventForwardingBroadcastReceiver(customEventInterstitialListener);
        mBroadcastReceiver.register(context);

        customEventInterstitialListener.onInterstitialLoaded();
    }

    @Override
    protected void onInvalidate() {
        mBroadcastReceiver.unregister();
    }

    private boolean extrasAreValid(Map<String,String> serverExtras) {
        return serverExtras.containsKey(HTML_RESPONSE_BODY_KEY);
    }
}
