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

package com.mopub.mobileads;

import android.app.Activity;
import com.mopub.mobileads.test.support.SdkTestRunner;
import com.mopub.mobileads.test.support.TestAdViewControllerFactory;
import com.mopub.mobileads.test.support.TestCustomEventBannerAdapterFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.*;

import static com.mopub.mobileads.MoPubErrorCode.ADAPTER_NOT_FOUND;
import static com.mopub.mobileads.util.ResponseHeader.CUSTOM_EVENT_DATA;
import static com.mopub.mobileads.util.ResponseHeader.CUSTOM_EVENT_HTML_DATA;
import static com.mopub.mobileads.util.ResponseHeader.CUSTOM_EVENT_NAME;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

@RunWith(SdkTestRunner.class)
public class MoPubViewTest {
    private MoPubView subject;
    private Map<String,String> paramsMap = new HashMap<String, String>();
    private CustomEventBannerAdapter customEventBannerAdapter;
    private AdViewController adViewController;

    @Before
    public void setup() {
        subject = new MoPubView(new Activity());
        customEventBannerAdapter = TestCustomEventBannerAdapterFactory.getSingletonMock();
        reset(customEventBannerAdapter);
        adViewController = TestAdViewControllerFactory.getSingletonMock();
    }

    @Test
    public void loadCustomEvent_shouldInitializeCustomEventBannerAdapter() throws Exception {
        paramsMap.put(CUSTOM_EVENT_NAME.getKey(), "name");
        paramsMap.put(CUSTOM_EVENT_DATA.getKey(), "data");
        paramsMap.put(CUSTOM_EVENT_HTML_DATA.getKey(), "html");
        subject.loadCustomEvent(paramsMap);

        assertThat(TestCustomEventBannerAdapterFactory.getLatestMoPubView()).isEqualTo(subject);
        assertThat(TestCustomEventBannerAdapterFactory.getLatestClassName()).isEqualTo("name");
        assertThat(TestCustomEventBannerAdapterFactory.getLatestClassData()).isEqualTo("data");

        verify(customEventBannerAdapter).loadAd();
    }

    @Test
    public void loadCustomEvent_whenParamsMapIsNull_shouldCallLoadFailUrl() throws Exception {
        subject.loadCustomEvent(null);

        verify(adViewController).loadFailUrl(eq(ADAPTER_NOT_FOUND));
        verify(customEventBannerAdapter, never()).invalidate();
        verify(customEventBannerAdapter, never()).loadAd();
    }
}
