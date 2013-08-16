package com.mopub.mobileads;

import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.revmob.RevMob;
import com.revmob.RevMobAdsListener;
import com.revmob.ads.fullscreen.RevMobFullscreen;

/*
 * 
 */
class RevMobInterstitial extends CustomEventInterstitial implements
		RevMobAdsListener {
	public static final String APP_ID_KEY = "appId";
	public static final String LOCATION_KEY = "location";

	private CustomEventInterstitialListener mInterstitialListener;
	private RevMobFullscreen fullscreenAd;

	/*
	 * Abstract methods from CustomEventInterstitial
	 */
	@Override
	protected void loadInterstitial(Context context,
			CustomEventInterstitialListener interstitialListener,
			Map<String, Object> localExtras, Map<String, String> serverExtras) {
		mInterstitialListener = interstitialListener;

		Activity activity = null;
		if (context instanceof Activity) {
			activity = (Activity) context;
		} else {
			// You may also pass in an Activity Context in the localExtras map
			// and retrieve it here.
		}

		if (activity == null) {
			mInterstitialListener
					.onInterstitialFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
			return;
		}

		Location location = extractLocation(localExtras);
		if (location != null) {
			RevMob.session().setUserLocation(location.getLatitude(), location.getLongitude(),
					location.getAccuracy());
		}

		// RevMob.session().setTestingMode(RevMobTestingMode.WITH_ADS);
		fullscreenAd = RevMob.session().createFullscreen(activity, this);
		fullscreenAd.load();
	}

	@Override
	protected void showInterstitial() {
		Log.d("MoPub", "Showing RevMob interstitial ad.");
		fullscreenAd.show();
	}

	@Override
	protected void onInvalidate() {
		fullscreenAd = null;
	}

	private Location extractLocation(Map<String, Object> localExtras) {
		Object location = localExtras.get(LOCATION_KEY);
		if (location instanceof Location) {
			return (Location) location;
		}
		return null;
	}

	// --
	@Override
	public void onRevMobAdClicked() {
		Log.d("MoPub", "RevMob interstitial ad clicked.");
		mInterstitialListener.onInterstitialClicked();

		this.onRevMobAdDismiss(); // must call this as Revmob doesn't trigger it
									// after clicking

	}

	@Override
	public void onRevMobAdDismiss() {
		Log.d("MoPub", "RevMob interstitial ad dismissed.");
		mInterstitialListener.onInterstitialDismissed();

	}

	@Override
	public void onRevMobAdDisplayed() {
		Log.d("MoPub", "RevMob interstitial ad displayed.");
		mInterstitialListener.onInterstitialShown();
	}

	@Override
	public void onRevMobAdNotReceived(String arg0) {
		Log.d("MoPub", "RevMob interstitial ad failed to load: " + arg0);
		mInterstitialListener
				.onInterstitialFailed(MoPubErrorCode.NETWORK_NO_FILL);

	}

	@Override
	public void onRevMobAdReceived() {
		Log.d("MoPub", "Revmob interstitial ad loaded successfully.");
		mInterstitialListener.onInterstitialLoaded();

	}
}
