package com.mopub.mobileads.factories;

import com.mopub.mobileads.AdFetcher;
import com.mopub.mobileads.AdView;

public class AdFetcherFactory {
    private static AdFetcherFactory instance = new AdFetcherFactory();

    public static void setInstance(AdFetcherFactory factory) {
        instance = factory;
    }

    public static AdFetcher create(AdView adView, String userAgent) {
        return instance.internalCreate(adView, userAgent);
    }

    public AdFetcher internalCreate(AdView adView, String userAgent) {
        return new AdFetcher(adView, userAgent);
    }
}
