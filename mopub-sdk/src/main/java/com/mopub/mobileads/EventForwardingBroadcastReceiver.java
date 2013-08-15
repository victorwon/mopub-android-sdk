package com.mopub.mobileads;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import static com.mopub.mobileads.BaseInterstitialActivity.ACTION_INTERSTITIAL_CLICK;
import static com.mopub.mobileads.BaseInterstitialActivity.ACTION_INTERSTITIAL_DISMISS;
import static com.mopub.mobileads.BaseInterstitialActivity.ACTION_INTERSTITIAL_FAIL;
import static com.mopub.mobileads.BaseInterstitialActivity.ACTION_INTERSTITIAL_SHOW;
import static com.mopub.mobileads.CustomEventInterstitial.CustomEventInterstitialListener;
import static com.mopub.mobileads.MoPubErrorCode.NETWORK_INVALID_STATE;

class EventForwardingBroadcastReceiver extends BroadcastReceiver {
    private final CustomEventInterstitialListener mCustomEventInterstitialListener;
    private Context mContext;

    public EventForwardingBroadcastReceiver(CustomEventInterstitialListener customEventInterstitialListener) {
        mCustomEventInterstitialListener = customEventInterstitialListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (mCustomEventInterstitialListener == null) {
            return;
        }

        String action = intent.getAction();
        if (action.equals(ACTION_INTERSTITIAL_FAIL)) {
            mCustomEventInterstitialListener.onInterstitialFailed(NETWORK_INVALID_STATE);
        } else if (action.equals(ACTION_INTERSTITIAL_SHOW)) {
            mCustomEventInterstitialListener.onInterstitialShown();
        } else if (action.equals(ACTION_INTERSTITIAL_DISMISS)) {
            mCustomEventInterstitialListener.onInterstitialDismissed();
        } else if (action.equals(ACTION_INTERSTITIAL_CLICK)) {
            mCustomEventInterstitialListener.onInterstitialClicked();
        }

    }

    public void register(Context context) {
        mContext = context;
        LocalBroadcastManager.getInstance(mContext).registerReceiver(this, BaseInterstitialActivity.HTML_INTERSTITIAL_INTENT_FILTER);
    }

    public void unregister() {
        if (mContext != null) {
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this);
            mContext = null;
        }
    }
}
