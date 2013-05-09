package com.mopub.mobileads;

import java.util.HashMap;
import java.util.Map;

public class AdTypeTranslator {
    private static Map<String, String> customEventNameForAdType = new HashMap<String, String>();

    static {
        customEventNameForAdType.put("admob_native_banner", "com.mopub.mobileads.GoogleAdMobBanner");
        customEventNameForAdType.put("admob_full_interstitial", "com.mopub.mobileads.GoogleAdMobInterstitial");
        customEventNameForAdType.put("millennial_native_banner", "com.mopub.mobileads.MillennialBanner");
        customEventNameForAdType.put("millennial_full_interstitial", "com.mopub.mobileads.MillennialInterstitial");
    }

    static String getCustomEventNameForAdType(String adType, String fullAdType) {
        String key = "interstitial".equals(adType)
                ? (fullAdType + "_interstitial")
                : (adType + "_banner");
        return customEventNameForAdType.get(key);
    }
}
