package com.mopub.mobileads;


import android.content.Intent;
import android.net.Uri;

import java.util.Map;

import static com.mopub.mobileads.AdFetcher.HTML_RESPONSE_BODY_KEY;

class MraidInterstitial extends ResponseBodyInterstitial {
    private String mHtmlData;

    @Override
    protected void extractExtras(Map<String, String> serverExtras) {
        mHtmlData = Uri.decode(serverExtras.get(HTML_RESPONSE_BODY_KEY));
    }

    @Override
    protected void showInterstitial() {
        Intent intent = new Intent(mContext, MraidActivity.class);
        intent.putExtra(HTML_RESPONSE_BODY_KEY, mHtmlData);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }
}
