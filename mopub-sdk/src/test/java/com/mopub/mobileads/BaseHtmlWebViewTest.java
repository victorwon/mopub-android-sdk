package com.mopub.mobileads;

import android.app.Activity;
import com.mopub.mobileads.test.support.SdkTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.shadows.ShadowWebView;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(SdkTestRunner.class)
public class BaseHtmlWebViewTest {

    private BaseHtmlWebView subject;

    @Before
    public void setUp() throws Exception {
        subject = new BaseHtmlWebView(new Activity());
    }

    @Test
    public void init_shouldSetWebViewScrollability() throws Exception {
        ShadowWebView shadowSubject = shadowOf(subject);

        subject.init(false);
        assertThat(shadowSubject.getOnTouchListener()).isNotNull();

        subject.init(true);
        assertThat(shadowSubject.getOnTouchListener()).isNull();
    }

    @Test
    public void loadUrl_shouldAcceptNullParameter() throws Exception {
        subject.loadUrl(null);
        // pass
    }

    @Test
    public void loadUrl_whenUrlIsJavascript_shouldCallSuperLoadUrl() throws Exception {
        String javascriptUrl = "javascript:function() {alert(\"guy\")};";
        subject.loadUrl(javascriptUrl);

        assertThat(shadowOf(subject).getLastLoadedUrl()).isEqualTo(javascriptUrl);
    }

    @Test
    public void loadHtmlResponse_shouldCallLoadDataWithBaseURL() throws Exception {
        String htmlResponse = "some random html response";
        subject.loadHtmlResponse(htmlResponse);

        ShadowWebView.LoadDataWithBaseURL lastLoadData = shadowOf(subject).getLastLoadDataWithBaseURL();
        assertThat(lastLoadData.baseUrl).isEqualTo("http://ads.mopub.com/");
        assertThat(lastLoadData.data).isEqualTo(htmlResponse);
        assertThat(lastLoadData.mimeType).isEqualTo("text/html");
        assertThat(lastLoadData.encoding).isEqualTo("utf-8");
        assertThat(lastLoadData.historyUrl).isNull();
    }
}
