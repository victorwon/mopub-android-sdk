package com.mopub.mobileads;

import android.app.Activity;
import android.content.Context;
import com.inmobi.commons.InMobi;
import com.inmobi.commons.InMobi.LOG_LEVEL;
import com.inmobi.mediation.adapter.inmobi.InMobiExtras;
import com.inmobi.monetization.IMBanner;
import com.inmobi.monetization.IMBannerListener;
import com.inmobi.monetization.IMErrorCode;
import com.mopub.mobileads.CustomEventBanner;
import com.mopub.mobileads.MoPub;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.util.Views;

import java.util.*;

/*
 * Tested with InMobi SDK 4.0.0
 */
public class InMobiBanner extends CustomEventBanner implements IMBannerListener {
    public static final String APP_ID_KEY = "appId";

	@Override
	protected void loadBanner(Context context,
			CustomEventBannerListener bannerListener,
			Map<String, Object> localExtras, Map<String, String> serverExtras) {
		mBannerListener = bannerListener;
        String inMobiAppId = serverExtras.get(APP_ID_KEY);

		Activity activity = null;
		if (context instanceof Activity) {
			activity = (Activity) context;
		} else {
			// You may also pass in an Activity Context in the localExtras map
			// and retrieve it here.
		}
		if (activity == null) {
			mBannerListener.onBannerFailed(null);
			return;
		}
		if (!isAppIntialize) {
			InMobi.initialize(activity, inMobiAppId);
			isAppIntialize = true;
		}

		/*
		 * You may also pass this String down in the serverExtras Map by
		 * specifying Custom Event Data in MoPub's web interface.
		 */
		iMBanner = new IMBanner(activity, inMobiAppId,
				IMBanner.INMOBI_AD_UNIT_320X50);

		Map<String, String> map = new HashMap<String, String>();
		InMobiExtras extras = new InMobiExtras();
		map.put("tp", "c_mopub");
		map.put("tp-ver", MoPub.SDK_VERSION);
		extras.setRequestParams(map);
		iMBanner.addNetworkExtras(extras);
		InMobi.setLogLevel(LOG_LEVEL.VERBOSE);
		iMBanner.setIMBannerListener(this);
		iMBanner.setRefreshInterval(-1);
		iMBanner.loadBanner();

	}

	private CustomEventBannerListener mBannerListener;
	private IMBanner iMBanner;
	private static boolean isAppIntialize = false;

	/*
	 * Abstract methods from CustomEventBanner
	 */

	@Override
	public void onInvalidate() {
		iMBanner.setIMBannerListener(null);
		if (iMBanner != null) {
            Views.removeFromParent(iMBanner);
            iMBanner.destroy();
		}
	}

	@Override
	public void onBannerInteraction(IMBanner imBanner, Map<String, String> map) {
		mBannerListener.onBannerClicked();
	}

	@Override
	public void onBannerRequestFailed(IMBanner imBanner, IMErrorCode imErrorCode) {

		if (imErrorCode == IMErrorCode.INTERNAL_ERROR) {
			mBannerListener.onBannerFailed(MoPubErrorCode.INTERNAL_ERROR);
		} else if (imErrorCode == IMErrorCode.INVALID_REQUEST) {
			mBannerListener
					.onBannerFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
		} else if (imErrorCode == IMErrorCode.NETWORK_ERROR) {
			mBannerListener
					.onBannerFailed(MoPubErrorCode.NETWORK_INVALID_STATE);
		} else if (imErrorCode == IMErrorCode.NO_FILL) {
			mBannerListener.onBannerFailed(MoPubErrorCode.NO_FILL);
		} else {
			mBannerListener.onBannerFailed(MoPubErrorCode.UNSPECIFIED);
		}
	}

	@Override
	public void onBannerRequestSucceeded(IMBanner imBanner) {
		if (iMBanner != null) {
			mBannerListener.onBannerLoaded(imBanner);

		} else {
			mBannerListener.onBannerFailed(null);
		}
	}

	@Override
	public void onDismissBannerScreen(IMBanner imBanner) {
		mBannerListener.onBannerCollapsed();
	}

	@Override
	public void onLeaveApplication(IMBanner imBanner) {

	}

	@Override
	public void onShowBannerScreen(IMBanner imBanner) {
		mBannerListener.onBannerExpanded();
	}

}
