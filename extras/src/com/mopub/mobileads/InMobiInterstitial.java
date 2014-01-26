package com.mopub.mobileads;

import android.app.Activity;
import android.content.Context;
import com.inmobi.commons.InMobi;
import com.inmobi.mediation.adapter.inmobi.InMobiExtras;
import com.inmobi.monetization.IMErrorCode;
import com.inmobi.monetization.IMInterstitial;
import com.inmobi.monetization.IMInterstitialListener;
import com.mopub.mobileads.CustomEventInterstitial;
import com.mopub.mobileads.MoPub;
import com.mopub.mobileads.MoPubErrorCode;

import java.util.*;

/*
 * Tested with InMobi SDK  4.0.0
 */
public class InMobiInterstitial extends CustomEventInterstitial implements IMInterstitialListener {
    public static final String APP_ID_KEY = "appId";

	@Override
	protected void loadInterstitial(Context context,
			CustomEventInterstitialListener interstitialListener,
			Map<String, Object> localExtras, Map<String, String> serverExtras) {
		mInterstitialListener = interstitialListener;
        String inMobiAppId = serverExtras.get(APP_ID_KEY);

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
		if (!isAppIntialize) {
			InMobi.initialize(activity, inMobiAppId);
			isAppIntialize = true;
		}
		this.iMInterstitial = new IMInterstitial(activity, inMobiAppId);

		Map<String, String> map = new HashMap<String, String>();
		InMobiExtras extras = new InMobiExtras();
		map.put("tp", "c_mopub");
		map.put("tp-ver", MoPub.SDK_VERSION);
		extras.setRequestParams(map);
		iMInterstitial.addNetworkExtras(extras);
		iMInterstitial.setIMInterstitialListener(this);
		iMInterstitial.loadInterstitial();
	}

	private CustomEventInterstitialListener mInterstitialListener;
	private IMInterstitial iMInterstitial;
	private static boolean isAppIntialize = false;

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
		iMInterstitial.setIMInterstitialListener(null);
		if (iMInterstitial != null) {
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
