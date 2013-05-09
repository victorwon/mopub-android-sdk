package com.mopub.simpleadsdemo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubView;
import com.mopub.mobileads.MoPubView.BannerAdListener;

public class BannersTab extends Activity implements BannerAdListener {
    private EditText mSearchText;
    private Button mSearchButton;
    private MoPubView mMRectBanner;
    private MoPubView mBanner;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.banners);

        // Initialize Ad components
        mMRectBanner = (MoPubView) findViewById(R.id.mrectview);
        mMRectBanner.setAdUnitId(SimpleAdsDemoConstants.PUB_ID_300x250);
        mMRectBanner.loadAd();

        mBanner = (MoPubView) findViewById(R.id.bannerview);
        mBanner.setAdUnitId(SimpleAdsDemoConstants.PUB_ID_320x50);
        mBanner.setBannerAdListener(this);
        mBanner.loadAd();

        mSearchText = (EditText) findViewById(R.id.searchtext);
        mSearchButton = (Button) findViewById(R.id.searchbutton);
        mSearchButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                InputMethodManager imm
                        = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mSearchText.getWindowToken(), 0);
                mMRectBanner.setKeywords(mSearchText.getText().toString());
                mBanner.setKeywords(mSearchText.getText().toString());

                mMRectBanner.loadAd();
                mBanner.loadAd();
            }
        });
    }
    
    @Override
    protected void onDestroy() {
        mBanner.destroy();
        mMRectBanner.destroy();
        super.onDestroy();
    }

    @Override
    public void onBannerLoaded(MoPubView banner) {
        Log.d("MoPub Demo", "Banner loaded callback.");
    }

    @Override
    public void onBannerFailed(MoPubView banner, MoPubErrorCode errorCode) {
        Log.d("MoPub Demo", "Banner failed callback with: " + errorCode.toString());
    }

    @Override
    public void onBannerClicked(MoPubView banner) {
        Log.d("MoPub Demo", "Banner clicked callback.");
    }

    @Override
    public void onBannerExpanded(MoPubView banner) {
        Log.d("MoPub Demo", "Banner expanded callback.");
    }

    @Override
    public void onBannerCollapsed(MoPubView banner) {
        Log.d("MoPub Demo", "Banner collapsed callback.");
    }
}