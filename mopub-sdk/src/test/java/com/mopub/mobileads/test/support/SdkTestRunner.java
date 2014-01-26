/*
 * Copyright (c) 2010-2013, MoPub Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *  Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 *  Neither the name of 'MoPub Inc.' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.mopub.mobileads.test.support;

import com.mopub.mobileads.factories.AdFetcherFactory;
import com.mopub.mobileads.factories.AdViewControllerFactory;
import com.mopub.mobileads.factories.CustomEventBannerAdapterFactory;
import com.mopub.mobileads.factories.CustomEventBannerFactory;
import com.mopub.mobileads.factories.CustomEventInterstitialAdapterFactory;
import com.mopub.mobileads.factories.CustomEventInterstitialFactory;
import com.mopub.mobileads.factories.HtmlBannerWebViewFactory;
import com.mopub.mobileads.factories.HtmlInterstitialWebViewFactory;
import com.mopub.mobileads.factories.HttpClientFactory;
import com.mopub.mobileads.factories.MoPubViewFactory;
import com.mopub.mobileads.factories.MraidViewFactory;
import com.mopub.mobileads.factories.VastManagerFactory;
import com.mopub.mobileads.factories.VastVideoDownloadTaskFactory;
import com.mopub.mobileads.factories.ViewGestureDetectorFactory;
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
            ViewGestureDetectorFactory.setInstance(new TestViewGestureDetectorFactory());
            VastManagerFactory.setInstance(new TestVastManagerFactory());
            VastVideoDownloadTaskFactory.setInstance(new TestVastVideoDownloadTaskFactory());

            MockitoAnnotations.initMocks(test);
        }
    }
}
