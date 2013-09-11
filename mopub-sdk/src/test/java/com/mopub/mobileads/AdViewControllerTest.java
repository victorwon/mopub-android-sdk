package com.mopub.mobileads;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;
import com.mopub.mobileads.factories.HtmlBannerWebViewFactory;
import com.mopub.mobileads.factories.HtmlInterstitialWebViewFactory;
import com.mopub.mobileads.factories.HttpClientFactory;
import com.mopub.mobileads.test.support.*;
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
import static com.mopub.mobileads.AdViewController.MINIMUM_REFRESH_TIME_MILLISECONDS;
import static com.mopub.mobileads.MoPubErrorCode.INTERNAL_ERROR;
import static com.mopub.mobileads.MoPubErrorCode.NO_FILL;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Fail.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
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

    @Before
    public void setup() {
        moPubView = mock(MoPubView.class);
        httpClient = HttpClientFactory.create();
        Activity context = new Activity();
        shadowOf(context).grantPermissions(ACCESS_NETWORK_STATE);
        subject = new AdViewController(context, moPubView);
        response = new TestHttpResponseWithHeaders(200, "I ain't got no-body");
        adFetcher = TestAdFetcherFactory.getSingletonMock();
    }

    @Test
    public void initialization_shouldInitializeWebViewFactories() throws Exception {
        new HtmlBannerWebViewFactory().internalCreate(null, false, "", "");
        new HtmlInterstitialWebViewFactory().internalCreate(null, false, "", "");

        // pass
    }

    @Test
    public void configureUsingHttpResponse_shouldSetFields() throws Exception {
        response.addHeader("X-Launchpage", "redirect url");
        response.addHeader("X-Clickthrough", "clickthrough url");
        response.addHeader("X-Width", "320  ");
        response.addHeader("X-Height", "  50");
        response.addHeader("X-AdTimeout", "  12  ");
        response.addHeader("X-Refreshtime", "70");

        subject.configureUsingHttpResponse(response);

        assertThat(subject.getRedirectUrl()).isEqualTo("redirect url");
        assertThat(subject.getClickthroughUrl()).isEqualTo("clickthrough url");
        assertThat(subject.getAdWidth()).isEqualTo(320);
        assertThat(subject.getAdHeight()).isEqualTo(50);
        assertThat(subject.getAdTimeoutDelay()).isEqualTo(12);
        assertThat(subject.getRefreshTimeMilliseconds()).isEqualTo(70000);
    }

    @Test
    public void configureUsingHttpResponse_withFloatTimeoutDelay_shouldTruncateTimeoutDelay() throws Exception {
        response.addHeader("X-AdTimeout", "3.14");
        subject.configureUsingHttpResponse(response);
        assertThat(subject.getAdTimeoutDelay()).isEqualTo(3);

        response = new TestHttpResponseWithHeaders(200, "I ain't got no-body");
        response.addHeader("X-AdTimeout", "-3.14");
        subject.configureUsingHttpResponse(response);
        assertThat(subject.getAdTimeoutDelay()).isEqualTo(-3);
    }

    @Test
    public void configureUsingHttpResponse_withInvalidTimeoutDelay_shouldSetAdTimeoutDelayToNull() throws Exception {
        // no X-AdTimeout header
        subject.configureUsingHttpResponse(response);
        assertThat(subject.getAdTimeoutDelay()).isNull();

        response = new TestHttpResponseWithHeaders(200, "I ain't got no-body");
        response.addHeader("X-AdTimeout", "not a number, i promise");
        subject.configureUsingHttpResponse(response);
        assertThat(subject.getAdTimeoutDelay()).isNull();
    }

    @Test
    public void configureUsingHttpResponse_shouldSetRefreshTimeToMinimumOf10Seconds() throws Exception {
        response.addHeader("X-Refreshtime", "0");

        subject.configureUsingHttpResponse(response);
        assertThat(subject.getRefreshTimeMilliseconds()).isEqualTo(MINIMUM_REFRESH_TIME_MILLISECONDS);
    }

    @Test
    public void configureUsingHttpResponse_whenRefreshTimeNotSpecified_shouldResetRefreshTimeTo0Seconds() throws Exception {
        response.addHeader("X-Refreshtime", "5");
        subject.configureUsingHttpResponse(response);

        assertThat(subject.getRefreshTimeMilliseconds()).isEqualTo(MINIMUM_REFRESH_TIME_MILLISECONDS);
        response = new TestHttpResponseWithHeaders(200, "I ain't got no-body");
        // no X-Refreshtime header
        subject.configureUsingHttpResponse(response);

        assertThat(subject.getRefreshTimeMilliseconds()).isEqualTo(0);
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
        String expectedUserAgent = new WebView(subject.getContext()).getSettings().getUserAgentString();
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
            Utils.invokeInstanceMethod(connectionManager, "assertStillUp");
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
        String expectedUserAgent = new WebView(subject.getContext()).getSettings().getUserAgentString();
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
            Utils.invokeInstanceMethod(connectionManager, "assertStillUp");
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

    @Test(expected = NullPointerException.class)
    public void cleanup_shouldCleanupHtmlBannerWebViewFactory() throws Exception {
        subject.cleanup();

        new HtmlBannerWebViewFactory().internalCreate(null, false, "", "");
    }

    @Test(expected = NullPointerException.class)
    public void cleanup_shouldCleanupHtmlInterstitialWebViewFactory() throws Exception {
        subject.cleanup();

        new HtmlInterstitialWebViewFactory().internalCreate(null, false, "", "");
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

    @Test
    public void cleanup_whenOtherAdViewControllersAreActive_shouldNotDisableTheWebViewPool() throws Exception {
        AdViewController anotherAdViewController = new AdViewController(new Activity(), moPubView);
        subject.cleanup();

        assertThat(TestHtmlBannerWebViewFactory.getWebViewPool().getNextHtmlWebView(null, true, "", "")).isNotNull();
        assertThat(TestHtmlInterstitialWebViewFactory.getWebViewPool().getNextHtmlWebView(null, true, "", "")).isNotNull();

        anotherAdViewController.cleanup();
        try {
            TestHtmlBannerWebViewFactory.getWebViewPool().getNextHtmlWebView(null, true, "", "");
            fail("Expected getNextHtmlWebView to fail");
        } catch(NullPointerException e) {
            // success!
        }
        try {
            TestHtmlInterstitialWebViewFactory.getWebViewPool().getNextHtmlWebView(null, true, "", "");
            fail("Expected getNextHtmlWebView to fail");
        } catch(NullPointerException e) {
            // success!
        }
    }
}
