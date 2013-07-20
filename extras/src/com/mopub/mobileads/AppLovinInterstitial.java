package com.mopub.mobileads;

import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.applovin.adview.AppLovinAdView;
import com.applovin.sdk.AppLovinAd;
import com.applovin.sdk.AppLovinAdDisplayListener;
import com.applovin.sdk.AppLovinAdLoadListener;
import com.applovin.sdk.AppLovinAdService;
import com.applovin.sdk.AppLovinAdSize;
import com.applovin.sdk.AppLovinSdk;

public class AppLovinInterstitial extends CustomEventInterstitial implements
		AppLovinAdLoadListener, AppLovinAdDisplayListener {
	public static final String LOCATION_KEY = "location";

	private CustomEventInterstitial.CustomEventInterstitialListener mInterstitialListener;
	private Activity parentActivity;
	private AppLovinAdService adService;
	private AppLovinAd lastReceived;

	/*
	 * Abstract methods from CustomEventInterstitial
	 */
	@Override
	public void loadInterstitial(
			Context context,
			CustomEventInterstitial.CustomEventInterstitialListener interstitialListener,
			Map<String, Object> localExtras, Map<String, String> serverExtras) {
		mInterstitialListener = interstitialListener;

		if (context instanceof Activity) {
			parentActivity = (Activity) context;
		} else {
			mInterstitialListener
					.onInterstitialFailed(MoPubErrorCode.INTERNAL_ERROR);
			return;
		}

		adService = AppLovinSdk.getInstance(context).getAdService();
		adService.loadNextAd(AppLovinAdSize.INTERSTITIAL, this);

		Log.d("AppLovinAdapter", "Interstitial loaded.");
	}

	@Override
	public void showInterstitial() {
		final AppLovinAd adToRender = lastReceived;

		if (adToRender != null) {
			Log.d("MoPub", "Showing AppLovin interstitial ad...");

			parentActivity.runOnUiThread(new Runnable() {
				public void run() {
					AppLovinAdView adView = new AppLovinAdView(
							AppLovinAdSize.BANNER, parentActivity);
					adView.renderAd(adToRender);

					mInterstitialListener.onInterstitialShown();
				}
			});
		}
	}

	@Override
	public void onInvalidate() {

	}

	@Override
	public void adReceived(AppLovinAd ad) {
		Log.d("MoPub", "AppLovin interstitial loaded successfully.");

		lastReceived = ad;

		parentActivity.runOnUiThread(new Runnable() {
			public void run() {
				mInterstitialListener.onInterstitialLoaded();

			}
		});
	}

	@Override
	public void failedToReceiveAd(int errorCode) {
		if (errorCode == 202) {
			mInterstitialListener.onInterstitialFailed(MoPubErrorCode.NO_FILL);
		} else if (errorCode >= 500) {
			mInterstitialListener
					.onInterstitialFailed(MoPubErrorCode.SERVER_ERROR);
		} else if (errorCode < 0) {
			mInterstitialListener
					.onInterstitialFailed(MoPubErrorCode.INTERNAL_ERROR);
		} else {
			mInterstitialListener
					.onInterstitialFailed(MoPubErrorCode.UNSPECIFIED);
		}
	}

	@Override
	public void adDisplayed(AppLovinAd arg0) {
		mInterstitialListener.onInterstitialShown();

	}

	@Override
	public void adHidden(AppLovinAd arg0) {
		mInterstitialListener.onInterstitialDismissed();

	}

}