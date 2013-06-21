package com.mopub.mobileads;

import java.util.HashMap;
import java.util.Map;

public class AdTypeTranslator {
    public static final String ADMOB_BANNER = "com.mopub.mobileads.GoogleAdMobBanner";
    public static final String ADMOB_INTERSTITIAL = "com.mopub.mobileads.GoogleAdMobInterstitial";
    public static final String MILLENNIAL_BANNER = "com.mopub.mobileads.MillennialBanner";
    public static final String MILLENNIAL_INTERSTITIAL = "com.mopub.mobileads.MillennialInterstitial";
    public static final String MRAID_BANNER = "com.mopub.mobileads.MraidBanner";
    public static final String MRAID_INTERSTITIAL = "com.mopub.mobileads.MraidInterstitial";
    public static final String HTML_BANNER = "com.mopub.mobileads.HtmlBanner";
    public static final String HTML_INTERSTITIAL = "com.mopub.mobileads.HtmlInterstitial";
    private static Map<String, String> customEventNameForAdType = new HashMap<String, String>();

    static {
        customEventNameForAdType.put("admob_native_banner", ADMOB_BANNER);
        customEventNameForAdType.put("admob_full_interstitial", ADMOB_INTERSTITIAL);
        customEventNameForAdType.put("millennial_native_banner", MILLENNIAL_BANNER);
        customEventNameForAdType.put("millennial_full_interstitial", MILLENNIAL_INTERSTITIAL);
        customEventNameForAdType.put("mraid_banner", MRAID_BANNER);
        customEventNameForAdType.put("mraid_interstitial", MRAID_INTERSTITIAL);
        customEventNameForAdType.put("html_banner", HTML_BANNER);
        customEventNameForAdType.put("html_interstitial", HTML_INTERSTITIAL);
    }

    static String getCustomEventNameForAdType(MoPubView moPubView, String adType, String fullAdType) {
        if ("html".equals(adType) || "mraid".equals(adType)) {
            return isInterstitial(moPubView)
                   ? customEventNameForAdType.get(adType + "_interstitial")
                   : customEventNameForAdType.get(adType + "_banner");
        } else {
            return "interstitial".equals(adType)
                    ? customEventNameForAdType.get(fullAdType + "_interstitial")
                    : customEventNameForAdType.get(adType + "_banner");
        }
    }

    private static boolean isInterstitial(MoPubView moPubView) {
        return moPubView instanceof MoPubInterstitial.MoPubInterstitialView;
    }
}
