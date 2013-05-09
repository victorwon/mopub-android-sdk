package com.mopub.mobileads;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import com.mopub.mobileads.factories.AdFetcherFactory;
import com.mopub.mobileads.factories.HttpClientFactory;
import com.mopub.mobileads.test.support.SdkTestRunner;
import com.mopub.mobileads.test.support.TestHttpResponseWithHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.tester.org.apache.http.FakeHttpLayer;

import java.lang.reflect.InvocationTargetException;

import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Fail.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.Robolectric.application;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(SdkTestRunner.class)
public class AdViewTest {
    private AdView subject;
    private MoPubView moPubView;
    private AdFetcher adFetcher;
    private HttpResponse response;
    private HttpClient httpClient;

    @Before
    public void setup() {
        moPubView = mock(MoPubView.class);
        adFetcher = AdFetcherFactory.create(null, null); // todo how do we say in code that this is a singleton mock?
        httpClient = HttpClientFactory.create(); // todo a real singleton HttpClient
        Activity context = new Activity();
        shadowOf(context).grantPermissions(ACCESS_NETWORK_STATE);
        subject = new AdView(context, moPubView);
        response = new TestHttpResponseWithHeaders(200, "I ain't got no-body");
    }

    @Test
    public void configureUsingHttpResponse_shouldSetFields() throws Exception {
        response.addHeader("X-Launchpage", "redirect url");
        response.addHeader("X-Clickthrough", "clickthrough url");
        response.addHeader("X-Scrollable", "0"); // todo test this with "1" and with nothing
        response.addHeader("X-Width", "320  ");
        response.addHeader("X-Height", "  50");
        response.addHeader("X-Refreshtime", "70");

        subject.configureUsingHttpResponse(response);

        assertThat(subject.getRedirectUrl()).isEqualTo("redirect url");
        assertThat(subject.getClickthroughUrl()).isEqualTo("clickthrough url");
        assertThat(Robolectric.shadowOf(subject).getOnTouchListener()).isNotNull();
        assertThat(subject.getAdWidth()).isEqualTo(320);
        assertThat(subject.getAdHeight()).isEqualTo(50);
        assertThat(subject.getRefreshTimeMilliseconds()).isEqualTo(70000);
    }

    @Test
    public void configureUsingHttpResponse_shouldHaveNullTouchListenerWhenScrollableIsOne() throws Exception {
        response.addHeader("X-Scrollable", "1");

        subject.configureUsingHttpResponse(response);
        assertThat(Robolectric.shadowOf(subject).getOnTouchListener()).isNull();
    }

    // todo Is this really the correct behavior
    @Test
    public void configureUsingHttpResponse_shouldHaveNullTouchListenerWhenScrollableNotSet() throws Exception {
        subject.configureUsingHttpResponse(response);
        assertThat(Robolectric.shadowOf(subject).getOnTouchListener()).isNull();
    }

    @Test
    public void configureUsingHttpResponse_shouldSetRefreshTimeToMinimumOf10Seconds() throws Exception {
        response.addHeader("X-Refreshtime", "0");

        subject.configureUsingHttpResponse(response);
        assertThat(subject.getRefreshTimeMilliseconds()).isEqualTo(10000);
    }

    @Test
    public void configureUsingHttpResponse_whenRefreshTimeNotSpecified_shouldResetRefreshTimeTo0Seconds() throws Exception {
        response.addHeader("X-Refreshtime", "5");
        subject.configureUsingHttpResponse(response);

        assertThat(subject.getRefreshTimeMilliseconds()).isEqualTo(10000);
        response = new TestHttpResponseWithHeaders(200, "I ain't got no-body");
        // no X-Refreshtime header
        subject.configureUsingHttpResponse(response);

        assertThat(subject.getRefreshTimeMilliseconds()).isEqualTo(0);
    }

    @Test
    public void getFailUrl_whenFailUrlHasBeenProvided_shouldLoadTheUrl() throws Exception {
        response.addHeader("X-Failurl", "fail url");
        subject.configureUsingHttpResponse(response);

        subject.loadFailUrl(MoPubErrorCode.UNSPECIFIED);

        verify(adFetcher).fetchAdForUrl(eq("fail url"));
    }

    @Test
    public void getFailUrl_whenFailUrlIsNull_shouldTellMoPubViewThatAdFailed() throws Exception {
        subject.configureUsingHttpResponse(response);

        subject.loadFailUrl(MoPubErrorCode.UNSPECIFIED);

        verify(moPubView).adFailed(eq(MoPubErrorCode.NO_FILL));
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
        String expectedUserAgent = subject.getSettings().getUserAgentString();
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

    // todo since we changed the catch block in AdView.trackImpression() to catch Exception
    // this test for impressionUrl is unnecessary (since we're catching the NullPointerException)
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
        String expectedUserAgent = subject.getSettings().getUserAgentString();
        FakeHttpLayer fakeHttpLayer = Robolectric.getFakeHttpLayer();
        fakeHttpLayer.addPendingHttpResponse(200, "");

        assertThat(expectedUserAgent).isNotNull();

        subject.registerClick();
        Thread.sleep(50); // does this make the test flaky?

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

    // todo since we changed the catch block in AdView.registerClick() to catch Exception
    // this test for clickthroughUrl is unnecessary (since we're catching the NullPointerException)
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
                "&id=" +
                "&nv=" + MoPub.SDK_VERSION +
                "&udid=sha%3A" +
                "&z=-0700" +
                "&o=u" +
                "&sc_a=1.0" +
                "&mr=1" +
                "&mcc=" +
                "&mnc=" +
                "&iso=" +
                "&cn=" +
                "&ct=3" +
                "&av=1.0";

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

}
