package com.mopub.simpleadsdemo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;
import com.mopub.mobileads.MoPubInterstitial.InterstitialAdListener;

public class InterstitialsTab extends Activity implements InterstitialAdListener {

    private MoPubInterstitial mMoPubInterstitial;
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interstitials);

        mMoPubInterstitial = new MoPubInterstitial(this, SimpleAdsDemoConstants.PUB_ID_INTERSTITIAL);
        mMoPubInterstitial.setInterstitialAdListener(this);
        
        Button loadInterstitialButton = (Button) findViewById(R.id.load_interstitial);
        loadInterstitialButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Load interstitial.
                mMoPubInterstitial.load();
            }
        });
        
        Button showInterstitialButton = (Button) findViewById(R.id.show_interstitial);
        showInterstitialButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Show interstitial.
                if (mMoPubInterstitial.isReady()) {
                    mMoPubInterstitial.show();
                } else {
                    logToast("Interstitial was not ready. Try reloading.");
                }
            }
        });
    }
    
    @Override
    protected void onDestroy() {
        mMoPubInterstitial.destroy();
        super.onDestroy();
    }
    
    /*
     * MoPubInterstitial.MoPubInterstitialListener implementation
     */
    @Override
    public void onInterstitialLoaded(MoPubInterstitial interstitial) {
        logToast("Interstitial loaded successfully.");
    }

    @Override
    public void onInterstitialFailed(MoPubInterstitial interstitial, MoPubErrorCode errorCode) {
        logToast("Interstitial failed to load with error: " + errorCode.toString());
    }
    
    @Override
    public void onInterstitialShown(MoPubInterstitial interstitial) {
        logToast("Interstitial shown.");
    }

    @Override
    public void onInterstitialDismissed(MoPubInterstitial interstitial) {
        logToast("Interstitial dismissed.");
    }
    
    private void logToast(String message) {
        Log.d("MoPub Demo", message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
