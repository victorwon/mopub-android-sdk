package com.mopub.mobileads;

import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.applovin.adview.AppLovinAdView;
import com.applovin.sdk.AppLovinAd;
import com.applovin.sdk.AppLovinAdDisplayListener;
import com.applovin.sdk.AppLovinAdLoadListener;
import com.applovin.sdk.AppLovinAdSize;
import com.applovin.sdk.AppLovinSdk;

public class AppLovinBanner extends CustomEventBanner implements AppLovinAdLoadListener, AppLovinAdDisplayListener
{

    private CustomEventBanner.CustomEventBannerListener mBannerListener;
    private AppLovinAdView ALAdView;

    /*
     * Abstract methods from CustomEventBanner
     */
	@Override
	protected void loadBanner(Context context,
			CustomEventBannerListener bannerListener,
			Map<String, Object> localExtras, Map<String, String> serverExtras)
    {
        mBannerListener = bannerListener;

        Activity activity = null;
        if (context instanceof Activity)
        {
            activity = (Activity) context;
        }
        else
        {
            mBannerListener.onBannerFailed(MoPubErrorCode.INTERNAL_ERROR);
            return;
        }

        Log.d("AppLovinAdapter", "Reqeust received for new BANNER.");

        ALAdView = new AppLovinAdView(AppLovinSdk.getInstance(context), AppLovinAdSize.BANNER, activity);
        ALAdView.setAdLoadListener(this);
        ALAdView.setAdDisplayListener(this);
        ALAdView.loadNextAd();
    }

    @Override
    public void onInvalidate()
    {
        ALAdView.setAdLoadListener(null);
    }

    @Override
    public void adReceived(AppLovinAd ad)
    {
        mBannerListener.onBannerLoaded(ALAdView);
        Log.d("AppLovinAdapter", "AdView was passed to MoPub.");
    }

    @Override
    public void failedToReceiveAd(int errorCode)
    {
        if (errorCode == 202)
        {
            mBannerListener.onBannerFailed(MoPubErrorCode.NO_FILL);
        }
        else if (errorCode >= 500)
        {
            mBannerListener.onBannerFailed(MoPubErrorCode.SERVER_ERROR);
        }
        else if (errorCode < 0)
        {
            mBannerListener.onBannerFailed(MoPubErrorCode.INTERNAL_ERROR);
        }
        else
        {
            mBannerListener.onBannerFailed(MoPubErrorCode.UNSPECIFIED);
        }
    }

	@Override
	public void adDisplayed(AppLovinAd arg0) {
		
	}

	@Override
	public void adHidden(AppLovinAd arg0) {
		
	}


}
	