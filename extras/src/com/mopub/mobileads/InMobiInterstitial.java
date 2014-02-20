package com.mopub.simpleadsdemo;

import android.app.Activity;
import android.content.Context;
import com.inmobi.commons.InMobi;
import com.inmobi.monetization.IMErrorCode;
import com.inmobi.monetization.IMInterstitial;
import com.inmobi.monetization.IMInterstitialListener;
import com.mopub.mobileads.CustomEventInterstitial;
import com.mopub.mobileads.MoPub;
import com.mopub.mobileads.MoPubErrorCode;

import java.util.*;

/*
 * Tested with InMobi SDK  4.1.1
 */
public class InMobiInterstitial extends CustomEventInterstitial implements IMInterstitialListener {

	@Override
	protected void loadInterstitial(Context context,
			CustomEventInterstitialListener interstitialListener,
			Map<String, Object> localExtras, Map<String, String> serverExtras) {
		mInterstitialListener = interstitialListener;
		String inMobiAppId = "YOUR_INMOBI_APP_ID";

		Activity activity = null;
		if (context instanceof Activity) {
			activity = (Activity) context;
		} else {
			// You may also pass in an Activity Context in the localExtras map
			// and retrieve it here.
		}

		if (activity == null) {
			mInterstitialListener.onInterstitialFailed(MoPubErrorCode.UNSPECIFIED);
			return;
		}

		/*
		 * You may also pass this String down in the serverExtras Map by
		 * specifying Custom Event Data in MoPub's web interface.
		 */
		if (!isAppInitialized) {
			InMobi.initialize(activity, inMobiAppId);
            isAppInitialized = true;
		}
		this.iMInterstitial = new IMInterstitial(activity, inMobiAppId);

        Map<String, String> map = new HashMap<String, String>();
        map.put("tp", "c_mopub");
        map.put("tp-ver", MoPub.SDK_VERSION);
        iMInterstitial.setRequestParams(map);
		iMInterstitial.setIMInterstitialListener(this);
		iMInterstitial.loadInterstitial();
	}

	private CustomEventInterstitialListener mInterstitialListener;
	private IMInterstitial iMInterstitial;
	private static boolean isAppInitialized = false;

	/*
	 * Abstract methods from CustomEventInterstitial
	 */

	@Override
	public void showInterstitial() {
		if (iMInterstitial != null
				&& IMInterstitial.State.READY.equals(this.iMInterstitial.getState())) {
			iMInterstitial.show();
		}
	}

	@Override
	public void onInvalidate() {
		if (iMInterstitial != null) {
            iMInterstitial.setIMInterstitialListener(null);
			iMInterstitial.destroy();
		}
	}

	@Override
	public void onDismissInterstitialScreen(IMInterstitial imInterstitial) {
		mInterstitialListener.onInterstitialDismissed();
	}

	@Override
	public void onInterstitialFailed(IMInterstitial imInterstitial, IMErrorCode imErrorCode) {
		if (imErrorCode == IMErrorCode.INTERNAL_ERROR) {
			mInterstitialListener.onInterstitialFailed(MoPubErrorCode.INTERNAL_ERROR);
		} else if (imErrorCode == IMErrorCode.INVALID_REQUEST) {
			mInterstitialListener.onInterstitialFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
		} else if (imErrorCode == IMErrorCode.NETWORK_ERROR) {
			mInterstitialListener.onInterstitialFailed(MoPubErrorCode.NETWORK_INVALID_STATE);
		} else if (imErrorCode == IMErrorCode.NO_FILL) {
			mInterstitialListener.onInterstitialFailed(MoPubErrorCode.NO_FILL);
		} else {
			mInterstitialListener.onInterstitialFailed(MoPubErrorCode.UNSPECIFIED);
		}
	}

	@Override
	public void onInterstitialInteraction(IMInterstitial imInterstitial,
			Map<String, String> map) {
		mInterstitialListener.onInterstitialClicked();
	}

	@Override
	public void onInterstitialLoaded(IMInterstitial imInterstitial) {
		mInterstitialListener.onInterstitialLoaded();
	}

	@Override
	public void onLeaveApplication(IMInterstitial imInterstitial) {

	}

	@Override
	public void onShowInterstitialScreen(IMInterstitial imInterstitial) {
		mInterstitialListener.onInterstitialShown();
	}
}
