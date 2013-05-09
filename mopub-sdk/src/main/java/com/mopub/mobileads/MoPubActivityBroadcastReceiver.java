package com.mopub.mobileads;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

abstract class MoPubActivityBroadcastReceiver extends BroadcastReceiver {
    abstract void onHtmlInterstitialShown();
    abstract void onHtmlInterstitialDismissed();
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        
        if (action.equals(MoPubActivity.ACTION_INTERSTITIAL_SHOW)) {
            onHtmlInterstitialShown();
        } else if (action.equals(MoPubActivity.ACTION_INTERSTITIAL_DISMISS)) {
            onHtmlInterstitialDismissed();
        }
    }
}
