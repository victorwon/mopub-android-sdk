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
import android.content.Context;
import android.content.Intent;
import android.view.ViewGroup;
import android.webkit.WebViewClient;
import com.mopub.mobileads.resource.MraidJavascript;
import com.mopub.mobileads.test.support.SdkTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowWebView;

import java.util.*;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(SdkTestRunner.class)
public class MraidViewTest {
    private Context context;
    private AdConfiguration adConfiguration;
    private MraidDisplayController mraidDisplayController;

    private MraidView bannerSubject;
    private MraidView interstitialSubject;
    private WebViewClient bannerWebViewClient;
    private WebViewClient interstitialWebViewClient;

    @Before
    public void setUp() throws Exception {
        context = new Activity();
        adConfiguration = mock(AdConfiguration.class);
        mraidDisplayController = mock(MraidDisplayController.class);

        bannerSubject = new MraidView(context, adConfiguration);
        bannerSubject.setMraidDisplayController(mraidDisplayController);
        bannerWebViewClient = bannerSubject.getMraidWebViewClient();

        interstitialSubject = new MraidView(context, adConfiguration, MraidView.ExpansionStyle.ENABLED, MraidView.NativeCloseButtonStyle.ALWAYS_VISIBLE, MraidView.PlacementType.INTERSTITIAL);
        interstitialSubject.setMraidDisplayController(mraidDisplayController);
        interstitialWebViewClient = interstitialSubject.getMraidWebViewClient();
    }

    @Test
    public void loadHtmlData_whenDataIsNull_shouldNotBlowUp() throws Exception {
        MraidView mraidViewSpy = spy(bannerSubject);

        mraidViewSpy.loadHtmlData(null);

        // pass

        verify(mraidViewSpy, never()).loadDataWithBaseURL(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void loadHtmlData_shouldSendSimpleHtmlToLoadDataWithBaseUrl() throws Exception {
        MraidView mraidViewSpy = spy(bannerSubject);
        String htmlData = "<html></html>";

        mraidViewSpy.loadHtmlData(htmlData);

        verify(mraidViewSpy).loadDataWithBaseURL(anyString(), eq(htmlData), eq("text/html"), eq("UTF-8"), anyString());
    }


    @Test
    public void loadHtmlData_whenHtmlContainsHeadTag_shouldIncludeMraidJs() throws Exception {
        MraidView mraidViewSpy = spy(bannerSubject);
        String htmlData = "<html><head></head></html>";
        String expectedResult = "<html><head><script>" + MraidJavascript.JAVASCRIPT_SOURCE + "</script></head></html>";

        mraidViewSpy.loadHtmlData(htmlData);

        verify(mraidViewSpy).loadDataWithBaseURL(anyString(), eq(expectedResult), eq("text/html"), eq("UTF-8"), anyString());
    }

    @Test
    public void loadHtmlData_whenMissingHtmlBoilerplate_shouldAddItAndIncludeMraidJs() throws Exception {
        MraidView mraidViewSpy = spy(bannerSubject);
        String htmlData = "<a href='www.goat.com'>CLICK THIS LINK</a>";
        String expectedResult = "<html><head><script>" + MraidJavascript.JAVASCRIPT_SOURCE + "</script></head>" +
                "<body style='margin:0;padding:0;'><a href='www.goat.com'>CLICK THIS LINK</a></body></html>";

        mraidViewSpy.loadHtmlData(htmlData);

        verify(mraidViewSpy).loadDataWithBaseURL(anyString(), eq(expectedResult), eq("text/html"), eq("UTF-8"), anyString());
    }

    @Test
    public void shouldOverrideUrlLoading_withMraidCommandCreateCalendarEvent_forBanners_withUserClick_shouldOpenNewCalendarIntent() throws Exception {
        String url = "mraid://createCalendarEvent?description=hi&start=1";
        Map<String, String> expectedParams = new HashMap<String, String>(2);
        expectedParams.put("description", "hi");
        expectedParams.put("start", "1");

        bannerSubject.onUserClick();

        bannerWebViewClient.shouldOverrideUrlLoading(null, url);

        verify(mraidDisplayController).createCalendarEvent(eq(expectedParams));
    }

    @Test
    public void shouldOverrideUrlLoading_withMraidCommandCreateCalendarEvent_forInterstitials_withUserClick_shouldOpenNewCalendarIntent() throws Exception {
        String url = "mraid://createCalendarEvent?description=hi&start=1";
        Map<String, String> expectedParams = new HashMap<String, String>(2);
        expectedParams.put("description", "hi");
        expectedParams.put("start", "1");

        interstitialSubject.onUserClick();
        interstitialWebViewClient.shouldOverrideUrlLoading(null, url);

        verify(mraidDisplayController).createCalendarEvent(eq(expectedParams));
    }

    @Test
    public void shouldOverrideUrlLoading_withMraidCommandCreateCalendarEvent_forBanners_withoutUserClick_shouldNotOpenNewIntent() throws Exception {
        String url = "mraid://createCalendarEvent?description=Mayan%20Apocalypse%2FEnd%20of%20World&start=2013-08-16T20%3A00-04%3A00&interval=1&frequency=daily";

        assertThat(bannerSubject.wasClicked()).isFalse();
        bannerWebViewClient.shouldOverrideUrlLoading(null, url);

        verify(mraidDisplayController, never()).createCalendarEvent(any(Map.class));
    }

    @Test
    public void shouldOverrideUrlLoading_withMraidCommandCreateCalendarEvent_forInterstitials_withoutUserClick_shouldNotOpenNewIntent() throws Exception {
        String url = "mraid://createCalendarEvent?description=Mayan%20Apocalypse%2FEnd%20of%20World&start=2013-08-16T20%3A00-04%3A00&interval=1&frequency=daily";

        interstitialWebViewClient.shouldOverrideUrlLoading(null, url);

        verify(mraidDisplayController, never()).createCalendarEvent(any(Map.class));
    }

    @Test
    public void shouldOverrideUrlLoading_withMraidCommandPlayVideo_forBanners_withOutUserClick_shouldNotOpenNewIntent() throws Exception {
        String url = "mraid://playVideo?uri=something";

        bannerWebViewClient.shouldOverrideUrlLoading(null, url);

        verify(mraidDisplayController, never()).showVideo(anyString());
    }

    @Test
    public void shouldOverrideUrlLoading_withMraidCommandPlayVideo_forBanners_withUserClick_shouldOpenNewIntent() throws Exception {
        String url = "mraid://playVideo?uri=something";

        bannerSubject.onUserClick();

        bannerWebViewClient.shouldOverrideUrlLoading(null, url);

        verify(mraidDisplayController).showVideo(eq("something"));
    }

    @Test
    public void shouldOverrideUrlLoading_withMraidCommandPlayVideo_forInterstitials_shouldOpenNewIntent() throws Exception {
        String url = "mraid://playVideo?uri=something";

        assertThat(interstitialSubject.wasClicked()).isFalse();
        interstitialWebViewClient.shouldOverrideUrlLoading(null, url);

        verify(mraidDisplayController).showVideo(eq("something"));

        reset(mraidDisplayController);
        interstitialSubject.onUserClick();
        interstitialWebViewClient.shouldOverrideUrlLoading(null, url);

        verify(mraidDisplayController).showVideo(eq("something"));
    }

    @Test
    public void shouldOverrideUrlLoading_withRedirectUrl_withoutUserClick_shouldNotOpenNewIntentAndReturnFalse() throws Exception {
        String url = "http://www.blah.com";

        boolean consumeUrlLoading = bannerWebViewClient.shouldOverrideUrlLoading(null, url);

        Intent startedIntent = Robolectric.getShadowApplication().getNextStartedActivity();
        assertThat(startedIntent).isNull();
        assertThat(consumeUrlLoading).isFalse();
    }

    @Test
    public void shouldOverrideUrlLoading_withRedirectUrl_withUserClick_shouldOpenNewIntent() throws Exception {
        String url = "http://www.blah.com";
        bannerSubject.onUserClick();

        boolean consumeUrlLoading = bannerWebViewClient.shouldOverrideUrlLoading(null, url);

        Intent startedIntent = Robolectric.getShadowApplication().getNextStartedActivity();
        assertThat(startedIntent).isNotNull();
        assertThat(consumeUrlLoading).isTrue();
    }

    @Test
    public void destroy_shouldRemoveSelfFromParent_beforeCallingDestroy() throws Exception {
        ViewGroup parent = mock(ViewGroup.class);
        ShadowWebView shadow = shadowOf(bannerSubject);
        shadow.setMyParent(parent);

        bannerSubject.destroy();

        verify(parent).removeView(eq(bannerSubject));
        assertThat(shadow.wasDestroyCalled()).isTrue();
    }
}
