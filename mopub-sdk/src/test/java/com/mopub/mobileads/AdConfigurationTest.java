package com.mopub.mobileads;

import android.app.Activity;
import android.location.Location;
import android.os.Build;

import com.mopub.mobileads.test.support.SdkTestRunner;
import com.mopub.mobileads.test.support.TestDateAndTime;
import com.mopub.mobileads.test.support.TestHttpResponseWithHeaders;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;

import static com.mopub.mobileads.AdViewController.MINIMUM_REFRESH_TIME_MILLISECONDS;
import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(SdkTestRunner.class)
public class AdConfigurationTest {
    private static final String AD_UNIT_ID = "adUnitId";
    private static final String KEYWORDS = "keywords";
    private static final Location LOCATION = new Location("");
    private static final int WIDTH = 320;
    private static final int HEIGHT = 50;
    private static final String CLICK_THROUGH_URL = "clickThroughUrl";
    private static final int LOCATION_PRECISION = 3;
    private static final HashMap<String,Object> LOCAL_EXTRAS = new HashMap<String, Object>();
    private static final String RESPONSE_STRING = "responseString";
    private static final String USER_AGENT = "Mozilla/5.0 (Linux; U; Android 4.0.3; ko-kr; LG-L160L Build/IML74K) AppleWebkit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30";
    private static final String DEVICE_MODEL = Build.MANUFACTURER + " " + Build.MODEL;
    private static final String DEVICE_LOCALE = "en_US";
    private static final String PLATFORM = "Android";

    private AdConfiguration subject;
    private Activity context;
    private TestHttpResponseWithHeaders httpResponse;

    @Before
    public void setUp() throws Exception {
        context = new Activity();

        subject = new AdConfiguration(context);

        httpResponse = new TestHttpResponseWithHeaders(200, "I ain't got no-body");
    }

    @Test
    public void constructor_shouldPopulateDeviceSpecificData() throws Exception {
        assertThat(subject.getUserAgent()).isEqualTo(USER_AGENT);
        assertThat(subject.getPlatformVersion()).isEqualTo(Build.VERSION.SDK_INT);
        assertThat(subject.getDeviceModel()).isEqualTo(DEVICE_MODEL);
        assertThat(subject.getDeviceLocale()).isEqualTo(DEVICE_LOCALE);
        assertThat(subject.getPlatform()).isEqualTo(PLATFORM);
    }

    @Test
    public void addHttpResponse_shouldSetFields() throws Exception {
        httpResponse.addHeader("X-Launchpage", "redirect url");
        httpResponse.addHeader("X-Clickthrough", "clickthrough url");
        httpResponse.addHeader("X-Width", "320  ");
        httpResponse.addHeader("X-Height", "  50");
        httpResponse.addHeader("X-AdTimeout", "  12  ");
        httpResponse.addHeader("X-Refreshtime", "70");

        subject.addHttpResponse(httpResponse);

        assertThat(subject.getRedirectUrl()).isEqualTo("redirect url");
        assertThat(subject.getClickthroughUrl()).isEqualTo("clickthrough url");
        assertThat(subject.getWidth()).isEqualTo(320);
        assertThat(subject.getHeight()).isEqualTo(50);
        assertThat(subject.getAdTimeoutDelay()).isEqualTo(12);
        assertThat(subject.getRefreshTimeMilliseconds()).isEqualTo(70000);
    }

    @Test
    public void addHttpResponse_withFloatTimeoutDelay_shouldTruncateTimeoutDelay() throws Exception {
        httpResponse.addHeader("X-AdTimeout", "3.14");
        subject.addHttpResponse(httpResponse);
        assertThat(subject.getAdTimeoutDelay()).isEqualTo(3);

        httpResponse = new TestHttpResponseWithHeaders(200, "I ain't got no-body");
        httpResponse.addHeader("X-AdTimeout", "-3.14");
        subject.addHttpResponse(httpResponse);
        assertThat(subject.getAdTimeoutDelay()).isEqualTo(-3);
    }

    @Test
    public void addHttpResponse_withInvalidTimeoutDelay_shouldSetAdTimeoutDelayToNull() throws Exception {
        // no X-AdTimeout header
        subject.addHttpResponse(httpResponse);
        assertThat(subject.getAdTimeoutDelay()).isNull();

        httpResponse = new TestHttpResponseWithHeaders(200, "I ain't got no-body");
        httpResponse.addHeader("X-AdTimeout", "not a number, i promise");
        subject.addHttpResponse(httpResponse);
        assertThat(subject.getAdTimeoutDelay()).isNull();
    }

    @Test
    public void caddHttpResponsee_shouldSetRefreshTimeToMinimumOf10Seconds() throws Exception {
        httpResponse.addHeader("X-Refreshtime", "0");

        subject.addHttpResponse(httpResponse);
        assertThat(subject.getRefreshTimeMilliseconds()).isEqualTo(MINIMUM_REFRESH_TIME_MILLISECONDS);
    }

    @Test
    public void addHttpResponse_whenRefreshTimeNotSpecified_shouldResetRefreshTimeTo0Seconds() throws Exception {
        httpResponse.addHeader("X-Refreshtime", "5");
        subject.addHttpResponse(httpResponse);

        assertThat(subject.getRefreshTimeMilliseconds()).isEqualTo(MINIMUM_REFRESH_TIME_MILLISECONDS);
        httpResponse = new TestHttpResponseWithHeaders(200, "I ain't got no-body");
        // no X-Refreshtime header
        subject.addHttpResponse(httpResponse);

        assertThat(subject.getRefreshTimeMilliseconds()).isEqualTo(0);
    }

    @Test
    public void addHttpResponse_shouldUpdateTimeStamp() throws Exception {
        long before = subject.getTimeStamp();
        Thread.sleep(25);

        subject.addHttpResponse(httpResponse);

        long after = subject.getTimeStamp();

        assertThat(after).isGreaterThanOrEqualTo(before);
    }
}
