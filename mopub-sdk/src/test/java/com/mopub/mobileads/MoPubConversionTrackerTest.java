package com.mopub.mobileads;

import android.app.Activity;
import com.mopub.mobileads.test.support.SdkTestRunner;
import org.apache.http.HttpRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.tester.org.apache.http.FakeHttpLayer;
import org.robolectric.tester.org.apache.http.HttpRequestInfo;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(SdkTestRunner.class)
public class MoPubConversionTrackerTest {
    private MoPubConversionTracker subject;
    private Activity context;
    private FakeHttpLayer fakeHttpLayer;

    @Before
    public void setUp() throws Exception {
        subject = new MoPubConversionTracker();
        context = new Activity();
        fakeHttpLayer = Robolectric.getFakeHttpLayer();
    }

    @Test
    public void reportAppOpen_onValidHttpResponse_isIdempotent() throws Exception {
        fakeHttpLayer.addPendingHttpResponse(200, "doesn't matter what this is as long as it's not nothing");
        subject.reportAppOpen(context);
        assertThat(requestWasMade()).isTrue();

        fakeHttpLayer.addPendingHttpResponse(200, "doesn't matter what this is as long as it's not nothing");
        subject.reportAppOpen(context);
        assertThat(requestWasMade()).isFalse();
    }

    @Test
    public void reportAppOpen_onInvalidStatusCode_shouldMakeSecondRequest() throws Exception {
        fakeHttpLayer.addPendingHttpResponse(404, "doesn't matter what this is as long as it's not nothing");
        subject.reportAppOpen(context);
        assertThat(requestWasMade()).isTrue();

        fakeHttpLayer.addPendingHttpResponse(404, "doesn't matter what this is as long as it's not nothing");
        subject.reportAppOpen(context);
        assertThat(requestWasMade()).isTrue();
    }

    @Test
    public void reportAppOpen_onEmptyResponse_shouldMakeSecondRequest() throws Exception {
        fakeHttpLayer.addPendingHttpResponse(200, "");
        subject.reportAppOpen(context);
        assertThat(requestWasMade()).isTrue();

        fakeHttpLayer.addPendingHttpResponse(200, "");
        subject.reportAppOpen(context);
        assertThat(requestWasMade()).isTrue();
    }

    private boolean requestWasMade() throws Exception {
        String expectedUrl = new StringBuilder("http://ads.mopub.com/m/open")
                .append("?v=6")
                .append("&id=").append("com.mopub.mobileads")
                .append("&udid=sha%3A").append("")
                .append("&av=").append("1.0")
                .toString();

        Thread.sleep(500);
        HttpRequestInfo lastSentHttpRequestInfo = fakeHttpLayer.getLastSentHttpRequestInfo();
        if (lastSentHttpRequestInfo == null) {
            return false;
        }
        HttpRequest request = lastSentHttpRequestInfo.getHttpRequest();
        fakeHttpLayer.clearRequestInfos();
        return request.getRequestLine().getUri().equals(expectedUrl);
    }
}
