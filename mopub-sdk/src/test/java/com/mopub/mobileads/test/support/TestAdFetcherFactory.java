package com.mopub.mobileads.test.support;

import com.mopub.mobileads.AdFetcher;
import com.mopub.mobileads.AdView;
import com.mopub.mobileads.factories.AdFetcherFactory;

import static org.mockito.Mockito.mock;

public class TestAdFetcherFactory extends AdFetcherFactory {
    private AdFetcher mockFetcher = mock(AdFetcher.class);

    @Override
    public AdFetcher internalCreate(AdView adView, String userAgent) {
        return mockFetcher;
    }
}
