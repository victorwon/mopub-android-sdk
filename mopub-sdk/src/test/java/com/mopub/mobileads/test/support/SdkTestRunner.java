package com.mopub.mobileads.test.support;

import com.mopub.mobileads.factories.*;
import com.mopub.mobileads.util.DateAndTime;
import org.junit.runners.model.InitializationError;
import org.mockito.MockitoAnnotations;
import org.robolectric.DefaultTestLifecycle;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.TestLifecycle;

public class SdkTestRunner extends RobolectricTestRunner {

    public SdkTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    @Override
    protected Class<? extends TestLifecycle> getTestLifecycleClass() {
        return TestLifeCycleWithInjection.class;
    }

    public static class TestLifeCycleWithInjection extends DefaultTestLifecycle {
        @Override
        public void prepareTest(Object test) {
            AdFetcherFactory.setInstance(new TestAdFetcherFactory());
            HttpClientFactory.setInstance(new TestHttpClientFactory());
            DateAndTime.setInstance(new TestDateAndTime());
            CustomEventBannerFactory.setInstance(new TestCustomEventBannerFactory());
            CustomEventInterstitialFactory.setInstance(new TestCustomEventInterstitialFactory());
            CustomEventBannerAdapterFactory.setInstance(new TestCustomEventBannerAdapterFactory());
            MraidViewFactory.setInstance(new TestMraidViewFactory());
            MoPubViewFactory.setInstance(new TestMoPubViewFactory());
            CustomEventInterstitialAdapterFactory.setInstance(new TestCustomEventInterstitialAdapterFactory());
            HtmlBannerWebViewFactory.setInstance(new TestHtmlBannerWebViewFactory());
            HtmlInterstitialWebViewFactory.setInstance(new TestHtmlInterstitialWebViewFactory());
            AdViewControllerFactory.setInstance(new TestAdViewControllerFactory());

            MockitoAnnotations.initMocks(test);
        }
    }
}
