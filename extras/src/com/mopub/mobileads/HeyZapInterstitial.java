package com.mopub.mobileads;

import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.heyzap.sdk.ads.InterstitialOverlay;
import com.heyzap.sdk.ads.OnAdDisplayListener;

/*
 * 
 */
class HeyZapInterstitial extends CustomEventInterstitial implements OnAdDisplayListener {

	private CustomEventInterstitialListener mInterstitialListener;

	private Activity contextActivity;
	
	/*
	 * Abstract methods from CustomEventInterstitial
	 */
	@Override
	protected void loadInterstitial(Context context,
			CustomEventInterstitialListener interstitialListener,
			Map<String, Object> localExtras, Map<String, String> serverExtras) {
		Log.d("HeyZap","loadInterstitial");

		mInterstitialListener = interstitialListener;

		if (context instanceof Activity) {
			contextActivity = (Activity) context;
		} else {
			// You may also pass in an Activity Context in the localExtras map
			// and retrieve it here.
			contextActivity = null;
		}

		if (contextActivity == null) {
			mInterstitialListener
					.onInterstitialFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
			return;
		}

		/*
		 * Remember to call HeyzapLib.start(this, HeyzapLib.FLAG_NO_HEYZAP_INSTALL_SPLASH);
		 * in main activity's onCreate()
		 */
		// do not use load(context, listener) as it won't set listener after initial call
		InterstitialOverlay.setDisplayListener(this);

		if (InterstitialOverlay.isAvailable()) {
			// already
			this.onAvailable();
		} else {
			if	(InterstitialOverlay.getInstance() == null)
				InterstitialOverlay.load(context, this);
			InterstitialOverlay.fetch(); // fetch won't do anything if loading is in process.
		}

	}

	@Override
	protected void showInterstitial() {
		Log.d("HeyZap","showInterstitial");

		if (InterstitialOverlay.isAvailable()) {
			InterstitialOverlay.display(contextActivity);

	    }
		
	}

	@Override
	protected void onInvalidate() {
		InterstitialOverlay.setDisplayListener(null);
		contextActivity = null;
		
	}

	
	@Override
	public void onShow() {
		mInterstitialListener.onInterstitialShown();
		
	}

	@Override
	public void onClick() {
		mInterstitialListener.onInterstitialClicked();
		
	}

	@Override
	public void onHide() {
		mInterstitialListener.onInterstitialDismissed();
		
	}

	@Override
	public void onFailedToShow() {
		mInterstitialListener.onInterstitialFailed(MoPubErrorCode.INTERNAL_ERROR);
		
		Log.w("HeyZap","Failed to show");

	}

	@Override
	public void onAvailable() {
		Log.d("HeyZap","onAvailable");
		mInterstitialListener.onInterstitialLoaded();
		
	}

	@Override
	public void onFailedToFetch() {
		mInterstitialListener.onInterstitialFailed(MoPubErrorCode.NETWORK_NO_FILL);
		
		Log.w("HeyZap","Failed to fetch");

	}

	
}
