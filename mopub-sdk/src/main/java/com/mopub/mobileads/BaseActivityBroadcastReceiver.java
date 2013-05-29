package com.mopub.mobileads;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static com.mopub.mobileads.BaseActivity.ACTION_INTERSTITIAL_DISMISS;
import static com.mopub.mobileads.BaseActivity.ACTION_INTERSTITIAL_SHOW;

abstract class BaseActivityBroadcastReceiver extends BroadcastReceiver {
    abstract void onHtmlInterstitialShown();
    abstract void onHtmlInterstitialDismissed();
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        
        if (action.equals(ACTION_INTERSTITIAL_SHOW)) {
            onHtmlInterstitialShown();
        } else if (action.equals(ACTION_INTERSTITIAL_DISMISS)) {
            onHtmlInterstitialDismissed();
        }
    }
}
