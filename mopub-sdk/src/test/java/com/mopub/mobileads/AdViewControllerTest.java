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
import android.net.ConnectivityManager;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;
import com.mopub.mobileads.factories.HttpClientFactory;
import com.mopub.mobileads.test.support.SdkTestRunner;
import com.mopub.mobileads.test.support.TestAdFetcherFactory;
import com.mopub.mobileads.test.support.TestHttpResponseWithHeaders;
import com.mopub.mobileads.test.support.ThreadUtils;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.Robolectric;
import org.robolectric.tester.org.apache.http.FakeHttpLayer;

import java.lang.reflect.InvocationTargetException;

import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static com.mopub.mobileads.AdViewController.DEFAULT_REFRESH_TIME_MILLISECONDS;
import static com.mopub.mobileads.MoPubErrorCode.INTERNAL_ERROR;
import static com.mopub.mobileads.MoPubErrorCode.NO_FILL;
import static com.mopub.mobileads.util.Reflection.MethodBuilder;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Fail.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.verify;
import static org.robolectric.Robolectric.application;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(SdkTestRunner.class)
public class AdViewControllerTest {
    private AdViewController subject;
    private MoPubView moPubView;
    private HttpResponse response;
    private HttpClient httpClient;
    private AdFetcher adFetcher;
    private Activity context;

    @Before
    public void setup() {
        moPubView = mock(MoPubView.class);
        stub(moPubView.getContext()).toReturn(new Activity());
        httpClient = HttpClientFactory.create();
        context = new Activity();
        shadowOf(context).grantPermissions(ACCESS_NETWORK_STATE);
        subject = new AdViewController(context, moPubView);
        response = new TestHttpResponseWithHeaders(200, "I ain't got no-body");
        adFetcher = TestAdFetcherFactory.getSingletonMock();
    }

    @Test
    public void scheduleRefreshTimerIfEnabled_shouldCancelOldRefreshAndScheduleANewOne() throws Exception {
        response.addHeader("X-Refreshtime", "30");
        subject.configureUsingHttpResponse(response);
        Robolectric.pauseMainLooper();
        assertThat(Robolectric.getUiThreadScheduler().enqueuedTaskCount()).isEqualTo(0);

        subject.scheduleRefreshTimerIfEnabled();

        assertThat(Robolectric.getUiThreadScheduler().enqueuedTaskCount()).isEqualTo(1);

        subject.scheduleRefreshTimerIfEnabled();

        assertThat(Robolectric.getUiThreadScheduler().enqueuedTaskCount()).isEqualTo(1);
    }

    @Test
    public void scheduleRefreshTimer_shouldNotScheduleRefreshIfAutorefreshIsOff() throws Exception {
        response.addHeader("X-Refreshtime", "30");
        subject.configureUsingHttpResponse(response);

        Robolectric.pauseMainLooper();
        assertThat(Robolectric.getUiThreadScheduler().enqueuedTaskCount()).isEqualTo(0);

        subject.setAutorefreshEnabled(false);

        subject.scheduleRefreshTimerIfEnabled();

        assertThat(Robolectric.getUiThreadScheduler().enqueuedTaskCount()).isEqualTo(0);
    }

    @Test
    public void scheduleRefreshTimer_whenAdViewControllerNotConfiguredByResponse_shouldHaveDefaultRefreshTime() throws Exception {
        Robolectric.pauseMainLooper();
        assertThat(Robolectric.getUiThreadScheduler().enqueuedTaskCount()).isEqualTo(0);

        subject.scheduleRefreshTimerIfEnabled();
        assertThat(Robolectric.getUiThreadScheduler().enqueuedTaskCount()).isEqualTo(1);

        Robolectric.idleMainLooper(DEFAULT_REFRESH_TIME_MILLISECONDS - 1);
        assertThat(Robolectric.getUiThreadScheduler().enqueuedTaskCount()).isEqualTo(1);

        Robolectric.idleMainLooper(1);
        assertThat(Robolectric.getUiThreadScheduler().enqueuedTaskCount()).isEqualTo(0);
    }

    @Test
    public void scheduleRefreshTimer_shouldNotScheduleRefreshIfRefreshTimeIsZero() throws Exception {
//        response.addHeader("X-Refreshtime", "0");
        subject.configureUsingHttpResponse(response);
        Robolectric.pauseMainLooper();

        assertThat(Robolectric.getUiThreadScheduler().enqueuedTaskCount()).isEqualTo(0);

        subject.scheduleRefreshTimerIfEnabled();

        assertThat(Robolectric.getUiThreadScheduler().enqueuedTaskCount()).isEqualTo(0);
    }

    @Test
    public void trackImpression_shouldHttpGetTheImpressionUrl() throws Exception {
        response.addHeader("X-Imptracker", "http://trackingUrl");
        subject.configureUsingHttpResponse(response);
        String expectedUserAgent = new WebView(context).getSettings().getUserAgentString();
        FakeHttpLayer fakeHttpLayer = Robolectric.getFakeHttpLayer();
        fakeHttpLayer.addPendingHttpResponse(200, "");

        assertThat(expectedUserAgent).isNotNull();

        subject.trackImpression();
        Thread.sleep(300); // does this make the test flaky?

        HttpRequest request = fakeHttpLayer.getLastSentHttpRequestInfo().getHttpRequest();
        assertThat(request.getFirstHeader("User-Agent").getValue()).isEqualTo(expectedUserAgent);
        assertThat(request.getRequestLine().getUri()).isEqualTo("http://trackingUrl");

        ClientConnectionManager connectionManager = httpClient.getConnectionManager();
        try {
            new MethodBuilder(connectionManager, "assertStillUp").setAccessible().execute();
            fail("should have thrown an exception");
        } catch (InvocationTargetException expected) {
            assertThat(expected.getCause()).isInstanceOf(IllegalStateException.class);
        }
    }

    @Test
    public void trackImpression_shouldDoNothingIfImpressionUrlNotSpecified() throws Exception {
        subject.configureUsingHttpResponse(response);
        FakeHttpLayer fakeHttpLayer = Robolectric.getFakeHttpLayer();
        fakeHttpLayer.addPendingHttpResponse(200, "");

        subject.trackImpression();
        Thread.sleep(300); // does this make the test flaky?

        assertThat(fakeHttpLayer.getLastSentHttpRequestInfo()).isNull();
    }

    @Test
    public void registerClick_shouldHttpGetTheClickthroughUrl() throws Exception {
        response.addHeader("X-Clickthrough", "http://clickUrl");
        subject.configureUsingHttpResponse(response);
        String expectedUserAgent = new WebView(context).getSettings().getUserAgentString();
        FakeHttpLayer fakeHttpLayer = Robolectric.getFakeHttpLayer();
        fakeHttpLayer.addPendingHttpResponse(200, "");

        assertThat(expectedUserAgent).isNotNull();

        subject.registerClick();
        Thread.sleep(200); // does this make the test flaky?

        HttpRequest request = fakeHttpLayer.getLastSentHttpRequestInfo().getHttpRequest();
        assertThat(request.getFirstHeader("User-Agent").getValue()).isEqualTo(expectedUserAgent);
        assertThat(request.getRequestLine().getUri()).isEqualTo("http://clickUrl");

        ClientConnectionManager connectionManager = httpClient.getConnectionManager();
        try {
            new MethodBuilder(connectionManager, "assertStillUp").setAccessible().execute();
            fail("should have thrown an exception");
        } catch (InvocationTargetException expected) {
            assertThat(expected.getCause()).isInstanceOf(IllegalStateException.class);
        }
    }

    @Test
    public void trackImpression_shouldDoNothingIfClickthroughUrlNotSpecified() throws Exception {
        subject.configureUsingHttpResponse(response);
        FakeHttpLayer fakeHttpLayer = Robolectric.getFakeHttpLayer();
        fakeHttpLayer.addPendingHttpResponse(200, "");

        subject.registerClick();
        Thread.sleep(50); // does this make the test flaky?

        assertThat(fakeHttpLayer.getLastSentHttpRequestInfo()).isNull();
    }

    @Test
    public void generateAdUrl_shouldIncludeMinFields() throws Exception {
        String expectedAdUrl = "http://ads.mopub.com/m/ad" +
                "?v=6" +
                "&nv=" + MoPub.SDK_VERSION +
                "&dn=" + Build.MANUFACTURER +
                "%2C" + Build.MODEL +
                "%2C" + Build.PRODUCT +
                "&udid=sha%3A" +
                "&z=-0700" +
                "&o=u" +
                "&sc_a=1.0" +
                "&mr=1" +
                "&ct=3" +
                "&av=1.0" +
                "&android_perms_ext_storage=0";

        String adUrl = subject.generateAdUrl();

        assertThat(adUrl).isEqualTo(expectedAdUrl);
    }

    @Test
    public void loadAd_shouldNotLoadUrlIfAdUnitIdIsNull() throws Exception {
        FakeHttpLayer fakeHttpLayer = Robolectric.getFakeHttpLayer();

        subject.loadAd();

        assertThat(fakeHttpLayer.getLastSentHttpRequestInfo()).isNull();
    }

    @Test
    public void loadAd_shouldScheduleRefreshIfNoNetworkConnectivity() throws Exception {
        FakeHttpLayer fakeHttpLayer = Robolectric.getFakeHttpLayer();
        Robolectric.pauseMainLooper();
        ConnectivityManager connectivityManager = (ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE);
        shadowOf(connectivityManager.getActiveNetworkInfo()).setConnectionStatus(false);
        response.addHeader("X-Refreshtime", "30");
        subject.configureUsingHttpResponse(response);
        subject.setAdUnitId("adUnitId");

        assertThat(Robolectric.getUiThreadScheduler().enqueuedTaskCount()).isEqualTo(0);

        subject.loadAd();

        assertThat(Robolectric.getUiThreadScheduler().enqueuedTaskCount()).isEqualTo(1);
        assertThat(fakeHttpLayer.getLastSentHttpRequestInfo()).isNull();
    }

    @Test
    public void loadNonJavascript_shouldFetchAd() throws Exception {
        String url = "http://www.guy.com";
        subject.loadNonJavascript(url);

        verify(adFetcher).fetchAdForUrl(eq(url));
    }

    @Test
    public void loadNonJavascript_whenAlreadyLoading_shouldNotFetchAd() throws Exception {
        String url = "http://www.guy.com";
        subject.loadNonJavascript(url);
        reset(adFetcher);
        subject.loadNonJavascript(url);

        verify(adFetcher, never()).fetchAdForUrl(anyString());
    }

    @Test
    public void loadNonJavascript_shouldClearTheFailUrl() throws Exception {
        subject.setFailUrl("blarg:");
        subject.loadNonJavascript("http://www.goodness.com");
        reset(adFetcher);
        subject.loadFailUrl(null);

        verify(adFetcher, never()).fetchAdForUrl(anyString());
        verify(moPubView).adFailed(eq(NO_FILL));
    }

    @Test
    public void loadNonJavascript_shouldAcceptNullParameter() throws Exception {
        subject.loadNonJavascript(null);
        // pass
    }

    @Test
    public void reload_shouldReuseOldUrl() throws Exception {
        String url = "http://www.guy.com";
        subject.loadNonJavascript(url);
        subject.setNotLoading();
        reset(adFetcher);
        subject.reload();

        verify(adFetcher).fetchAdForUrl(eq(url));
    }

    @Test
    public void loadFailUrl_shouldLoadFailUrl() throws Exception {
        String failUrl = "http://www.bad.man";
        subject.setFailUrl(failUrl);
        subject.loadFailUrl(INTERNAL_ERROR);

        verify(adFetcher).fetchAdForUrl(eq(failUrl));
        verify(moPubView, never()).adFailed(any(MoPubErrorCode.class));
    }

    @Test
    public void loadFailUrl_shouldAcceptNullErrorCode() throws Exception {
        subject.loadFailUrl(null);
        // pass
    }

    @Test
    public void loadFailUrl_whenFailUrlIsNull_shouldCallAdDidFail() throws Exception {
        subject.setFailUrl(null);
        subject.loadFailUrl(INTERNAL_ERROR);

        verify(moPubView).adFailed(eq(NO_FILL));
        verify(adFetcher, never()).fetchAdForUrl(anyString());
    }

    @Test
    public void setAdContentView_whenCalledFromWrongUiThread_shouldStillSetContentView() throws Exception {
        response.addHeader("X-Width", "320");
        response.addHeader("X-Height", "50");
        final View view = mock(View.class);
        AdViewController.setShouldHonorServerDimensions(view);
        subject.configureUsingHttpResponse(response);

        new Thread(new Runnable() {
            @Override
            public void run() {
                subject.setAdContentView(view);
            }
        }).start();
        ThreadUtils.pause(10);
        Robolectric.runUiThreadTasks();

        verify(moPubView).removeAllViews();
        ArgumentCaptor<FrameLayout.LayoutParams> layoutParamsCaptor = ArgumentCaptor.forClass(FrameLayout.LayoutParams.class);
        verify(moPubView).addView(eq(view), layoutParamsCaptor.capture());
        FrameLayout.LayoutParams layoutParams = layoutParamsCaptor.getValue();

        assertThat(layoutParams.width).isEqualTo(320);
        assertThat(layoutParams.height).isEqualTo(50);
        assertThat(layoutParams.gravity).isEqualTo(Gravity.CENTER);
    }

    @Test
    public void setAdContentView_whenCalledAfterCleanUp_shouldNotRemoveViewsAndAddView() throws Exception {
        response.addHeader("X-Width", "320");
        response.addHeader("X-Height", "50");
        final View view = mock(View.class);
        AdViewController.setShouldHonorServerDimensions(view);
        subject.configureUsingHttpResponse(response);

        subject.cleanup();
        new Thread(new Runnable() {
            @Override
            public void run() {
                subject.setAdContentView(view);
            }
        }).start();
        ThreadUtils.pause(10);
        Robolectric.runUiThreadTasks();

        verify(moPubView, never()).removeAllViews();
        verify(moPubView, never()).addView(any(View.class), any(FrameLayout.LayoutParams.class));
    }

    @Test
    public void setAdContentView_whenHonorServerDimensionsAndHasDimensions_shouldSizeAndCenterView() throws Exception {
        response.addHeader("X-Width", "320");
        response.addHeader("X-Height", "50");
        View view = mock(View.class);
        AdViewController.setShouldHonorServerDimensions(view);
        subject.configureUsingHttpResponse(response);

        subject.setAdContentView(view);

        verify(moPubView).removeAllViews();
        ArgumentCaptor<FrameLayout.LayoutParams> layoutParamsCaptor = ArgumentCaptor.forClass(FrameLayout.LayoutParams.class);
        verify(moPubView).addView(eq(view), layoutParamsCaptor.capture());
        FrameLayout.LayoutParams layoutParams = layoutParamsCaptor.getValue();

        assertThat(layoutParams.width).isEqualTo(320);
        assertThat(layoutParams.height).isEqualTo(50);
        assertThat(layoutParams.gravity).isEqualTo(Gravity.CENTER);
    }

    @Test
    public void setAdContentView_whenHonorServerDimensionsAndDoesntHaveDimensions_shouldWrapAndCenterView() throws Exception {
        View view = mock(View.class);
        AdViewController.setShouldHonorServerDimensions(view);
        subject.configureUsingHttpResponse(response);

        subject.setAdContentView(view);

        verify(moPubView).removeAllViews();
        ArgumentCaptor<FrameLayout.LayoutParams> layoutParamsCaptor = ArgumentCaptor.forClass(FrameLayout.LayoutParams.class);
        verify(moPubView).addView(eq(view), layoutParamsCaptor.capture());
        FrameLayout.LayoutParams layoutParams = layoutParamsCaptor.getValue();

        assertThat(layoutParams.width).isEqualTo(FrameLayout.LayoutParams.WRAP_CONTENT);
        assertThat(layoutParams.height).isEqualTo(FrameLayout.LayoutParams.WRAP_CONTENT);
        assertThat(layoutParams.gravity).isEqualTo(Gravity.CENTER);
    }

    @Test
    public void setAdContentView_whenNotServerDimensions_shouldWrapAndCenterView() throws Exception {
        response.addHeader("X-Width", "320");
        response.addHeader("X-Height", "50");
        subject.configureUsingHttpResponse(response);
        View view = mock(View.class);

        subject.setAdContentView(view);

        verify(moPubView).removeAllViews();
        ArgumentCaptor<FrameLayout.LayoutParams> layoutParamsCaptor = ArgumentCaptor.forClass(FrameLayout.LayoutParams.class);
        verify(moPubView).addView(eq(view), layoutParamsCaptor.capture());
        FrameLayout.LayoutParams layoutParams = layoutParamsCaptor.getValue();

        assertThat(layoutParams.width).isEqualTo(FrameLayout.LayoutParams.WRAP_CONTENT);
        assertThat(layoutParams.height).isEqualTo(FrameLayout.LayoutParams.WRAP_CONTENT);
        assertThat(layoutParams.gravity).isEqualTo(Gravity.CENTER);
    }
}
