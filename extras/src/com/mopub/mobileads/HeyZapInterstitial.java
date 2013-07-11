package com.mopub.mobileads;

import java.util.Map;

import android.app.Activity;
import android.content.Context;

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
		 * Remember to call HeyzapLib.start(context, HeyzapLib.FLAG_NONE); in main activity's onCreate()
		 */
		InterstitialOverlay.setDisplayListener(this);
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
		
	}

	@Override
	public void onAvailable() {
		mInterstitialListener.onInterstitialLoaded();
		
	}

	@Override
	public void onFailedToFetch() {
		mInterstitialListener.onInterstitialFailed(MoPubErrorCode.NETWORK_NO_FILL);
		
	}

	@Override
	protected void showInterstitial() {
		InterstitialOverlay.display(contextActivity);
		
	}

	@Override
	protected void onInvalidate() {
		InterstitialOverlay.setDisplayListener(null);
		contextActivity = null;
		
	}

	
}
