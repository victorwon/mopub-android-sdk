package com.mopub.mobileads.test.support;

import com.mopub.mobileads.AdFetcher;
import com.mopub.mobileads.AdViewController;
import com.mopub.mobileads.factories.AdFetcherFactory;

import static org.mockito.Mockito.mock;

public class TestAdFetcherFactory extends AdFetcherFactory {
    private AdFetcher mockAdFetcher = mock(AdFetcher.class);

    public static AdFetcher getSingletonMock() {
        return ((TestAdFetcherFactory)AdFetcherFactory.instance).mockAdFetcher;
    }

    @Override
    public AdFetcher internalCreate(AdViewController adViewController, String userAgent) {
        return mockAdFetcher;
    }
}
