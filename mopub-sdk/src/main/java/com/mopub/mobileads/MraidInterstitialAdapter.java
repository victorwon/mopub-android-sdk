package com.mopub.mobileads;


import android.app.Activity;
import android.content.Intent;

class MraidInterstitialAdapter extends BaseInterstitialAdapter {
    @Override
    void loadInterstitial() {
        if (mAdapterListener != null) mAdapterListener.onNativeInterstitialLoaded(this);
    }

    @Override
    void showInterstitial() {
        Activity activity = mInterstitial.getActivity();
        Intent i = new Intent(activity, MraidActivity.class);
        i.putExtra("com.mopub.mobileads.Source", mJsonParams);
        activity.startActivity(i);
    }
}
