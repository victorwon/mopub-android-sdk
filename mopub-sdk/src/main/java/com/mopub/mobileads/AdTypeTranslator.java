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
    private static Map<String, String> customEventNameForAdType = new HashMap<String, String>();

    static {
        customEventNameForAdType.put("admob_native_banner", ADMOB_BANNER);
        customEventNameForAdType.put("admob_full_interstitial", ADMOB_INTERSTITIAL);
        customEventNameForAdType.put("millennial_native_banner", MILLENNIAL_BANNER);
        customEventNameForAdType.put("millennial_full_interstitial", MILLENNIAL_INTERSTITIAL);
        customEventNameForAdType.put("mraid_banner", MRAID_BANNER);
        customEventNameForAdType.put("mraid_interstitial", MRAID_INTERSTITIAL);
    }

    static String getCustomEventNameForAdType(String adType, String fullAdType) {
        String key = "interstitial".equals(adType)
                ? (fullAdType + "_interstitial")
                : (adType + "_banner");
        return customEventNameForAdType.get(key);
    }
}
