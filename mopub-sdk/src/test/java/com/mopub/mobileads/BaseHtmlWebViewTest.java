package com.mopub.mobileads;

import android.app.Activity;
import android.os.Build;
import com.mopub.mobileads.test.support.SdkTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowWebView;

import static android.webkit.WebSettings.PluginState;
import static com.mopub.mobileads.util.VersionCode.HONEYCOMB_MR2;
import static com.mopub.mobileads.util.VersionCode.ICE_CREAM_SANDWICH;
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
    public void shouldEnablePluginsBasedOnApiLevel() throws Exception {
        Robolectric.Reflection.setFinalStaticField(Build.VERSION.class, "SDK_INT", ICE_CREAM_SANDWICH.getApiLevel());
        subject = new BaseHtmlWebView(new Activity());
        assertThat(subject.getSettings().getPluginState()).isEqualTo(PluginState.ON);

        Robolectric.Reflection.setFinalStaticField(Build.VERSION.class, "SDK_INT", HONEYCOMB_MR2.getApiLevel());
        subject = new BaseHtmlWebView(new Activity());
        assertThat(subject.getSettings().getPluginState()).isEqualTo(PluginState.OFF);
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
