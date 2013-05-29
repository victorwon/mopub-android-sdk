package com.mopub.mobileads.factories;

import com.mopub.mobileads.AdFetcher;
import com.mopub.mobileads.AdViewController;

public class AdFetcherFactory {
    private static AdFetcherFactory instance = new AdFetcherFactory();

    public static void setInstance(AdFetcherFactory factory) {
        instance = factory;
    }

    public static AdFetcher create(AdViewController adViewController, String userAgent) {
        return instance.internalCreate(adViewController, userAgent);
    }

    public AdFetcher internalCreate(AdViewController adViewController, String userAgent) {
        return new AdFetcher(adViewController, userAgent);
    }
}
