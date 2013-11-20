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
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.webkit.WebView;
import com.mopub.mobileads.test.support.SdkTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.Robolectric;

import static com.mopub.mobileads.MoPubErrorCode.UNSPECIFIED;
import static com.mopub.mobileads.MraidBrowser.URL_EXTRA;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(SdkTestRunner.class)
public class HtmlWebViewClientTest {

    private HtmlWebViewClient subject;
    private HtmlWebViewListener htmlWebViewListener;
    private BaseHtmlWebView htmlWebView;

    @Before
    public void setUp() throws Exception {
        htmlWebViewListener = mock(HtmlWebViewListener.class);
        htmlWebView = mock(BaseHtmlWebView.class);
        stub(htmlWebView.getContext()).toReturn(new Activity());
        subject = new HtmlWebViewClient(htmlWebViewListener, htmlWebView, "clickthrough", "redirect");
    }

    @Test
    public void shouldOverrideUrlLoading_withMoPubFinishLoad_withUserClick_shouldCallAdDidLoad() throws Exception {
        stub(htmlWebView.wasClicked()).toReturn(true);

        boolean didOverrideUrl = subject.shouldOverrideUrlLoading(htmlWebView, "mopub://finishLoad");

        assertThat(didOverrideUrl).isTrue();
        verify(htmlWebViewListener).onLoaded(eq(htmlWebView));
    }

    @Test
    public void shouldOverrideUrlLoading_withMoPubFinishLoad_whenUserNotClicked_shouldCallAdDidLoad() throws Exception {
        stub(htmlWebView.wasClicked()).toReturn(false);

        boolean didOverrideUrl = subject.shouldOverrideUrlLoading(htmlWebView, "mopub://finishLoad");

        assertThat(didOverrideUrl).isTrue();
        verify(htmlWebViewListener).onLoaded(eq(htmlWebView));
    }

    @Test
    public void shouldOverrideUrlLoading_withMoPubClose_withUserClick_shouldCallAdDidClose() throws Exception {
        stub(htmlWebView.wasClicked()).toReturn(true);

        boolean didOverrideUrl = subject.shouldOverrideUrlLoading(htmlWebView, "mopub://close");

        assertThat(didOverrideUrl).isTrue();
        verify(htmlWebViewListener).onCollapsed();
    }

    @Test
    public void shouldOverrideUrlLoading_withMoPubClose_withoutUserClick_shouldCallAdDidClose() throws Exception {
        stub(htmlWebView.wasClicked()).toReturn(false);

        boolean didOverrideUrl = subject.shouldOverrideUrlLoading(htmlWebView, "mopub://close");

        assertThat(didOverrideUrl).isTrue();
        verify(htmlWebViewListener).onCollapsed();
    }

    @Test
    public void shouldOverrideUrlLoading_withMoPubFailLoad_shouldCallLoadFailUrl() throws Exception {
        stub(htmlWebView.wasClicked()).toReturn(true);

        boolean didOverrideUrl = subject.shouldOverrideUrlLoading(htmlWebView, "mopub://failLoad");

        assertThat(didOverrideUrl).isTrue();
        verify(htmlWebViewListener).onFailed(UNSPECIFIED);
    }

    @Test
    public void shouldOverrideUrlLoading_withMoPubCustom_withUserClick_shouldStartCustomIntent() throws Exception {
        stub(htmlWebView.wasClicked()).toReturn(true);

        boolean didOverrideUrl = subject.shouldOverrideUrlLoading(htmlWebView, "mopub://custom?fnc=myFnc&data=myData");

        assertThat(didOverrideUrl).isTrue();
        verify(htmlWebViewListener).onClicked();
        Intent startedActivity = Robolectric.getShadowApplication().getNextStartedActivity();
        assertThat(startedActivity).isNotNull();
        assertThat(startedActivity.getAction()).isEqualTo("myFnc");
        assertThat(startedActivity.getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK).isNotEqualTo(0);
        assertThat(startedActivity.getStringExtra(HtmlBannerWebView.EXTRA_AD_CLICK_DATA)).isEqualTo("myData");
    }

    @Test
    public void shouldOverrideUrlLoading_withMoPubCustom_withoutUserClick_shouldNotStartActivity() throws Exception {
        stub(htmlWebView.wasClicked()).toReturn(false);

        boolean didOverrideUrl = subject.shouldOverrideUrlLoading(htmlWebView, "mopub://custom?fnc=myFnc&data=myData");

        assertThat(didOverrideUrl).isTrue();
        verify(htmlWebViewListener, never()).onClicked();
        Intent startedActivity = Robolectric.getShadowApplication().getNextStartedActivity();
        assertThat(startedActivity).isNull();
    }

    @Test
    public void shouldOverrideUrlLoading_withMoPubCustomAndNullData_withUserClick_shouldStartCustomIntent() throws Exception {
        stub(htmlWebView.wasClicked()).toReturn(true);

        boolean didOverrideUrl = subject.shouldOverrideUrlLoading(htmlWebView, "mopub://custom?fnc=myFnc");

        assertThat(didOverrideUrl).isTrue();
        verify(htmlWebViewListener).onClicked();
        Intent startedActivity = Robolectric.getShadowApplication().getNextStartedActivity();
        assertThat(startedActivity).isNotNull();
        assertThat(startedActivity.getAction()).isEqualTo("myFnc");
        assertThat(startedActivity.getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK).isNotEqualTo(0);
        assertThat(startedActivity.getStringExtra(HtmlBannerWebView.EXTRA_AD_CLICK_DATA)).isNull();
    }

    @Test
    public void shouldOverrideUrlLoading_withMoPubCustomAndNullData_withoutUserClick_shouldNotStartCustomIntent() throws Exception {
        stub(htmlWebView.wasClicked()).toReturn(false);

        boolean didOverrideUrl = subject.shouldOverrideUrlLoading(htmlWebView, "mopub://custom?fnc=myFnc");

        assertThat(didOverrideUrl).isTrue();
        verify(htmlWebViewListener, never()).onClicked();
        Intent startedActivity = Robolectric.getShadowApplication().getNextStartedActivity();
        assertThat(startedActivity).isNull();
    }

    @Test
    public void shouldOverrideUrlLoading_withPhoneIntent_shouldStartDefaultIntent() throws Exception {
        assertPhoneUrlStartedCorrectIntent("tel:");
        assertPhoneUrlStartedCorrectIntent("voicemail:");
        assertPhoneUrlStartedCorrectIntent("sms:");
        assertPhoneUrlStartedCorrectIntent("mailto:");
        assertPhoneUrlStartedCorrectIntent("geo:");
        assertPhoneUrlStartedCorrectIntent("google.streetview:");
    }

    @Test
    public void shouldOverrideUrlLoading_withValidMarketIntent_withUserClick_shouldOpenPlayStore() throws Exception {
        stub(htmlWebView.wasClicked()).toReturn(true);

        subject = new HtmlWebViewClient(htmlWebViewListener, htmlWebView, null, null);
        String validMarketUrl = "market://somethingValid";
        Robolectric.packageManager.addResolveInfoForIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(validMarketUrl)), new ResolveInfo());
        boolean didOverrideUrl = subject.shouldOverrideUrlLoading(htmlWebView, validMarketUrl);

        assertThat(didOverrideUrl).isTrue();
        verify(htmlWebViewListener).onClicked();

        Intent startedActivity = assertActivityStarted();
        assertThat(startedActivity.getAction()).isEqualTo(Intent.ACTION_VIEW);
        assertThat(startedActivity.getData().toString()).isEqualTo(validMarketUrl);
    }

    @Test
    public void shouldOverrideUrlLoading_withValidMarketIntent_withoutUserClick_shouldNotOpenPlayStore() throws Exception {
        stub(htmlWebView.wasClicked()).toReturn(false);

        subject = new HtmlWebViewClient(htmlWebViewListener, htmlWebView, null, null);
        String validMarketUrl = "market://somethingValid";
        Robolectric.packageManager.addResolveInfoForIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(validMarketUrl)), new ResolveInfo());
        boolean didOverrideUrl = subject.shouldOverrideUrlLoading(htmlWebView, validMarketUrl);

        assertThat(didOverrideUrl).isTrue();
        verify(htmlWebViewListener, never()).onClicked();

        assertThat(Robolectric.getShadowApplication().getNextStartedActivity()).isNull();
    }

    @Test
    public void shouldOverrideUrlLoading_withUnhandleableMarketIntent_withUserClick_shouldNotOpenBrowser() throws Exception {
        String invalidMarketUrl = "market://somethingInvalid";
        stub(htmlWebView.wasClicked()).toReturn(true);
        boolean didOverrideUrl = subject.shouldOverrideUrlLoading(htmlWebView, invalidMarketUrl);

        assertThat(didOverrideUrl).isTrue();
        verify(htmlWebViewListener, never()).onClicked();

        assertThat(Robolectric.getShadowApplication().getNextStartedActivity()).isNull();
    }

    @Test
    public void shouldOverrideUrlLoading_withUnhandleableMarketIntent_withoutUserClick_shouldNotOpenBrowser() throws Exception {
        String invalidMarketUrl = "market://somethingInvalid";
        stub(htmlWebView.wasClicked()).toReturn(false);
        boolean didOverrideUrl = subject.shouldOverrideUrlLoading(htmlWebView, invalidMarketUrl);

        assertThat(didOverrideUrl).isTrue();
        verify(htmlWebViewListener, never()).onClicked();

        assertThat(Robolectric.getShadowApplication().getNextStartedActivity()).isNull();
    }

    @Test
    public void shouldOverrideUrlLoading_withAmazonIntentAndAmazonPresent_withUserClick_shouldOpenAmazonMarket() throws Exception {
        stub(htmlWebView.wasClicked()).toReturn(true);

        subject = new HtmlWebViewClient(htmlWebViewListener, htmlWebView, null, null);
        String validAmazonUrl = "amzn://somethingValid";
        Robolectric.packageManager.addResolveInfoForIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(validAmazonUrl)), new ResolveInfo());
        boolean didOverrideUrl = subject.shouldOverrideUrlLoading(htmlWebView, validAmazonUrl);

        assertThat(didOverrideUrl).isTrue();
        verify(htmlWebViewListener).onClicked();

        Intent startedActivity = assertActivityStarted();
        assertThat(startedActivity.getAction()).isEqualTo(Intent.ACTION_VIEW);
        assertThat(startedActivity.getData().toString()).isEqualTo(validAmazonUrl);
    }

    @Test
    public void shouldOverrideUrlLoading_withAmazonIntentAndAmazonPresent_withoutUserClick_shouldNotOpenAmazonMarket() throws Exception {
        stub(htmlWebView.wasClicked()).toReturn(false);

        subject = new HtmlWebViewClient(htmlWebViewListener, htmlWebView, null, null);
        String validAmazonUrl = "amzn://somethingValid";
        Robolectric.packageManager.addResolveInfoForIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(validAmazonUrl)), new ResolveInfo());
        boolean didOverrideUrl = subject.shouldOverrideUrlLoading(htmlWebView, validAmazonUrl);

        assertThat(didOverrideUrl).isTrue();
        verify(htmlWebViewListener, never()).onClicked();

        assertThat(Robolectric.getShadowApplication().getNextStartedActivity()).isNull();
    }

    @Test
    public void shouldOverrideUrlLoading_withAmazonIntentAndNoAmazon_withUserClick_shouldNotTryToOpenAmazonMarket() throws Exception {
        String invalidAmazonUrl = "amzn://somethingValid";
        stub(htmlWebView.wasClicked()).toReturn(true);
        boolean didOverrideUrl = subject.shouldOverrideUrlLoading(htmlWebView, invalidAmazonUrl);

        assertThat(didOverrideUrl).isTrue();
        verify(htmlWebViewListener, never()).onClicked();

        Intent startedActivity = Robolectric.getShadowApplication().getNextStartedActivity();
        assertThat(startedActivity).isNull();
    }

    @Test
    public void shouldOverrideUrlLoading_withAmazonIntentAndNoAmazon_withoutUserClick_shouldNotTryToOpenAmazonMarket() throws Exception {
        String invalidAmazonUrl = "amzn://somethingValid";
        stub(htmlWebView.wasClicked()).toReturn(false);
        boolean didOverrideUrl = subject.shouldOverrideUrlLoading(htmlWebView, invalidAmazonUrl);

        assertThat(didOverrideUrl).isTrue();
        verify(htmlWebViewListener, never()).onClicked();

        assertThat(Robolectric.getShadowApplication().getNextStartedActivity()).isNull();
    }

    @Test
    public void shouldOverrideUrlLoading_withHttpUrl_withUserClick_shouldOpenBrowser() throws Exception {
        subject = new HtmlWebViewClient(htmlWebViewListener, htmlWebView, null, null);
        stub(htmlWebView.wasClicked()).toReturn(true);
        String validUrl = "http://www.mopub.com";
        boolean didOverrideUrl = subject.shouldOverrideUrlLoading(htmlWebView, validUrl);

        assertThat(didOverrideUrl).isTrue();
        verify(htmlWebViewListener).onClicked();

        Intent startedActivity = assertActivityStarted();
        assertThat(startedActivity.getComponent().getClassName()).isEqualTo("com.mopub.mobileads.MraidBrowser");
        assertThat(startedActivity.getStringExtra(URL_EXTRA)).isEqualTo(validUrl);
        assertThat(startedActivity.getData()).isNull();
    }

    @Test
    public void shouldOverrideUrlLoading_withHttpUrl_withoutUserClick_shouldNotOpenBrowser() throws Exception {
        subject = new HtmlWebViewClient(htmlWebViewListener, htmlWebView, null, null);
        stub(htmlWebView.wasClicked()).toReturn(false);
        String validUrl = "http://www.mopub.com";
        boolean didOverrideUrl = subject.shouldOverrideUrlLoading(htmlWebView, validUrl);

        assertThat(didOverrideUrl).isTrue();
        verify(htmlWebViewListener, never()).onClicked();

        assertThat(Robolectric.getShadowApplication().getNextStartedActivity()).isNull();
    }

    @Test
    public void shouldOverrideUrlLoading_withClickTrackingRedirect_withUserClick_shouldChangeUrl() throws Exception {
        String validUrl = "http://www.mopub.com";
        stub(htmlWebView.wasClicked()).toReturn(true);

        subject.shouldOverrideUrlLoading(htmlWebView, validUrl);

        Intent startedActivity = assertActivityStarted();
        assertThat(startedActivity.getStringExtra(URL_EXTRA)).isEqualTo("clickthrough&r=http%3A%2F%2Fwww.mopub.com");
    }

    @Test
    public void shouldOverrideUrlLoading_withClickTrackingRedirect_withoutUserClick_shouldChangeUrl() throws Exception {
        String validUrl = "http://www.mopub.com";
        stub(htmlWebView.wasClicked()).toReturn(false);

        subject.shouldOverrideUrlLoading(htmlWebView, validUrl);

        assertThat(Robolectric.getShadowApplication().getNextStartedActivity()).isNull();
    }

    @Test
    public void shouldOverrideUrlLoading_withEmptyUrl_withUserClick_shouldLoadAboutBlank() throws Exception {
        stub(htmlWebView.wasClicked()).toReturn(true);
        subject = new HtmlWebViewClient(htmlWebViewListener, htmlWebView, null, null);

        subject.shouldOverrideUrlLoading(htmlWebView, "");

        Intent startedActivity = assertActivityStarted();
        assertThat(startedActivity.getComponent().getClassName()).isEqualTo("com.mopub.mobileads.MraidBrowser");
        assertThat(startedActivity.getStringExtra(URL_EXTRA)).isEqualTo("about:blank");
        assertThat(startedActivity.getData()).isNull();
    }

    @Test
    public void shouldOverrideUrlLoading_withEmptyUrl_withoutUserClick_shouldLoadAboutBlank() throws Exception {
        stub(htmlWebView.wasClicked()).toReturn(false);
        subject = new HtmlWebViewClient(htmlWebViewListener, htmlWebView, null, null);

        subject.shouldOverrideUrlLoading(htmlWebView, "");

        assertThat(Robolectric.getShadowApplication().getNextStartedActivity()).isNull();
    }

    @Test
    public void shouldOverrideUrlLoading_withNativeBrowserScheme_withUserClick_shouldStartIntentWithActionView() throws Exception {
        stub(htmlWebView.wasClicked()).toReturn(true);
        subject = new HtmlWebViewClient(htmlWebViewListener, htmlWebView, null, null);

        subject.shouldOverrideUrlLoading(htmlWebView, "mopubnativebrowser://navigate?url=http://mopub.com");

        Intent startedActivity = assertActivityStarted();
        assertThat(isWebsiteUrl(startedActivity.getData().toString()));
        assertThat(startedActivity.getAction()).isEqualTo("android.intent.action.VIEW");
        verify(htmlWebViewListener).onClicked();
    }

    @Test
    public void shouldOverrideUrlLoading_withNativeBrowserScheme_withoutUserClick_shouldStartIntentWithActionView() throws Exception {
        stub(htmlWebView.wasClicked()).toReturn(false);
        subject = new HtmlWebViewClient(htmlWebViewListener, htmlWebView, null, null);

        subject.shouldOverrideUrlLoading(htmlWebView, "mopubnativebrowser://navigate?url=http://mopub.com");

        verify(htmlWebViewListener, never()).onClicked();
        assertThat(Robolectric.getShadowApplication().getNextStartedActivity()).isNull();
    }

    @Test
    public void shouldOverrideUrlLoading_withNativeBrowserScheme_butOpaqueUri_withUserClick_shouldNotBeHandledByNativeBrowser() throws Exception {
        stub(htmlWebView.wasClicked()).toReturn(true);
        String opaqueNativeBrowserUriString = "mopubnativebrowser:navigate?url=http://mopub.com";
        subject = new HtmlWebViewClient(htmlWebViewListener, htmlWebView, null, null);

        subject.shouldOverrideUrlLoading(htmlWebView, opaqueNativeBrowserUriString);

        Intent startedActivity = assertActivityStarted();
        assertThat(startedActivity.getComponent().getClassName()).isEqualTo("com.mopub.mobileads.MraidBrowser");
        assertThat(startedActivity.getStringExtra(URL_EXTRA)).isEqualTo(opaqueNativeBrowserUriString);
        assertThat(startedActivity.getData()).isNull();
        verify(htmlWebViewListener).onClicked();
    }

    @Test
    public void shouldOverrideUrlLoading_withNativeBrowserScheme_butOpaqueUri_withoutUserClick_shouldNotLoad() throws Exception {
        stub(htmlWebView.wasClicked()).toReturn(false);
        String opaqueNativeBrowserUriString = "mopubnativebrowser:navigate?url=http://mopub.com";
        subject = new HtmlWebViewClient(htmlWebViewListener, htmlWebView, null, null);

        subject.shouldOverrideUrlLoading(htmlWebView, opaqueNativeBrowserUriString);

        assertThat(Robolectric.getShadowApplication().getNextStartedActivity()).isNull();
    }

    @Test
    public void shouldOverrideUrlLoading_withNativeBrowserScheme_withInvalidHostSchemeUrl_withUserClick_shouldNotInvokeNativeBrowser() throws Exception {
        stub(htmlWebView.wasClicked()).toReturn(true);
        subject = new HtmlWebViewClient(htmlWebViewListener, htmlWebView, null, null);

        subject.shouldOverrideUrlLoading(htmlWebView, "something://blah?url=invalid");

        Intent startedActivity = assertActivityStarted();
        assertThat(startedActivity.getAction()).isNotEqualTo("android.intent.action.VIEW");
        verify(htmlWebViewListener).onClicked();
    }

    @Test
    public void shouldOverrideUrlLoading_withNativeBrowserScheme_withInvalidHostSchemeUrl_withoutUserClick_shouldNotInvokeNativeBrowser() throws Exception {
        stub(htmlWebView.wasClicked()).toReturn(false);
        subject = new HtmlWebViewClient(htmlWebViewListener, htmlWebView, null, null);

        subject.shouldOverrideUrlLoading(htmlWebView, "something://blah?url=invalid");

        assertThat(Robolectric.getShadowApplication().getNextStartedActivity()).isNull();
    }

    private boolean isWebsiteUrl(String url){
        return url.startsWith("http://") || url.startsWith("https://");
    }

    @Test
    public void onPageStarted_whenLoadedUrlStartsWithRedirect_withUserClick_shouldOpenInBrowser() throws Exception {
        String url = "redirectUrlToLoad";
        stub(htmlWebView.wasClicked()).toReturn(true);
        subject = new HtmlWebViewClient(htmlWebViewListener, htmlWebView, null, "redirect");
        WebView view = mock(WebView.class);
        subject.onPageStarted(view, url, null);

        verify(view).stopLoading();

        Intent intent = Robolectric.getShadowApplication().getNextStartedActivity();
        assertThat(intent.getStringExtra(MraidBrowser.URL_EXTRA)).isEqualTo(url);
        assertThat(intent.getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK).isNotEqualTo(0);
        assertThat(intent.getComponent().getClassName()).isEqualTo("com.mopub.mobileads.MraidBrowser");
    }

    @Test
    public void onPageStarted_whenLoadedUrlStartsWithRedirect_withoutUserClick_shouldOpenInBrowser() throws Exception {
        String url = "redirectUrlToLoad";
        stub(htmlWebView.wasClicked()).toReturn(false);
        subject = new HtmlWebViewClient(htmlWebViewListener, htmlWebView, null, "redirect");
        WebView view = mock(WebView.class);
        subject.onPageStarted(view, url, null);

        verify(view).stopLoading();

        assertThat(Robolectric.getShadowApplication().getNextStartedActivity()).isNull();
    }

    @Test
    public void onPageStarted_whenLoadedUrlStartsWithRedirectAndHasClickthrough_withUserClick_shouldOpenInBrowser() throws Exception {
        stub(htmlWebView.wasClicked()).toReturn(true);
        String url = "redirectUrlToLoad";
        String expectedTrackingUrl = "clickthrough" + "&r=" + url;
        WebView view = mock(WebView.class);
        subject.onPageStarted(view, url, null);

        verify(view).stopLoading();

        Intent intent = Robolectric.getShadowApplication().getNextStartedActivity();
        assertThat(intent.getStringExtra(MraidBrowser.URL_EXTRA)).isEqualTo(expectedTrackingUrl);
        assertThat(intent.getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK).isNotEqualTo(0);
        assertThat(intent.getComponent().getClassName()).isEqualTo("com.mopub.mobileads.MraidBrowser");
    }

    @Test
    public void onPageStarted_whenLoadedUrlStartsWithRedirectAndHasClickthrough_withoutUserClick_shouldNotOpenInBrowser() throws Exception {
        stub(htmlWebView.wasClicked()).toReturn(false);
        String url = "redirectUrlToLoad";
        WebView view = mock(WebView.class);
        subject.onPageStarted(view, url, null);

        verify(view).stopLoading();

        assertThat(Robolectric.getShadowApplication().getNextStartedActivity()).isNull();
    }

    @Test
    public void onPageStarted_whenLoadedUrlStartsWithRedirectAndHasClickthrough_withUserClick_whenMraidBrowserCannotHandleIntent_shouldOpenInNativeBrowser() throws Exception {
        Context mockContext = mock(Context.class);
        stub(htmlWebView.wasClicked()).toReturn(true);
        stub(htmlWebView.getContext()).toReturn(mockContext);
        String url = "redirectUrlToLoad";

        // We only want startActivity() to throw an exception the first time we call it.
        doThrow(new ActivityNotFoundException())
                .doNothing()
                .when(mockContext).startActivity(any(Intent.class));

        subject = new HtmlWebViewClient(htmlWebViewListener, htmlWebView, "clickthrough", "redirect");
        subject.onPageStarted(htmlWebView, url, null);

        ArgumentCaptor<Intent> intentArgumentCaptor = ArgumentCaptor.forClass(Intent.class);
        verify(mockContext, times(2)).startActivity(intentArgumentCaptor.capture());

        Intent intent = intentArgumentCaptor.getAllValues().get(1);
        assertThat(intent.getAction()).isEqualTo("android.intent.action.VIEW");
        assertThat(intent.getData().toString()).isEqualTo("about:blank");
        assertThat(intent.getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK).isNotEqualTo(0);
        verify(htmlWebViewListener, times(1)).onClicked();
    }

    @Test
    public void onPageStarted_whenLoadedUrlDoesntStartWithRedirect_shouldDoNothing() throws Exception {
        WebView view = mock(WebView.class);
        subject.onPageStarted(view, "this doesn't start with redirect", null);

        verify(view, never()).stopLoading();

        assertThat(Robolectric.getShadowApplication().getNextStartedActivity()).isNull();
    }

    @Test
    public void launchIntentForUserClick_shouldStartActivityAndResetClickStatusAndReturnTrue() throws Exception {
        stub(htmlWebView.wasClicked()).toReturn(true);
        Context context = mock(Context.class);
        Intent intent = mock(Intent.class);

        boolean result = subject.launchIntentForUserClick(context, intent, null);

        verify(context).startActivity(eq(intent));
        verify(htmlWebView).onResetUserClick();
        assertThat(result).isTrue();
    }

    @Test
    public void launchIntentForUserClick_whenUserHasNotClicked_shouldNotStartActivityAndReturnFalse() throws Exception {
        stub(htmlWebView.wasClicked()).toReturn(false);
        Context context = mock(Context.class);
        Intent intent = mock(Intent.class);

        boolean result = subject.launchIntentForUserClick(context, intent, null);

        verify(context, never()).startActivity(any(Intent.class));
        verify(htmlWebView, never()).onResetUserClick();
        assertThat(result).isFalse();
    }

    @Test
    public void launchIntentForUserClick_whenNoMatchingActivity_shouldNotStartActivityAndReturnFalse() throws Exception {
        Context context = mock(Context.class);
        Intent intent = mock(Intent.class);

        stub(htmlWebView.wasClicked()).toReturn(true);
        doThrow(new ActivityNotFoundException()).when(context).startActivity(any(Intent.class));

        boolean result = subject.launchIntentForUserClick(context, intent, null);

        verify(htmlWebView, never()).onResetUserClick();
        assertThat(result).isFalse();
    }

    @Test
    public void launchIntentForUserClick_whenContextIsNull_shouldNotStartActivityAndReturnFalse() throws Exception {
        stub(htmlWebView.wasClicked()).toReturn(true);
        Intent intent = new Intent();

        boolean result = subject.launchIntentForUserClick(null, intent, null);

        assertThat(Robolectric.getShadowApplication().getNextStartedActivity()).isNull();
        verify(htmlWebView, never()).onResetUserClick();
        assertThat(result).isFalse();
    }

    private void assertPhoneUrlStartedCorrectIntent(String url) {
        boolean didOverrideUrl;

        stub(htmlWebView.wasClicked()).toReturn(true);
        didOverrideUrl = subject.shouldOverrideUrlLoading(htmlWebView, url);
        Intent startedActivity = assertActivityStarted();
        assertThat(startedActivity.getAction()).isEqualTo(Intent.ACTION_VIEW);
        assertThat(startedActivity.getData().toString()).isEqualTo(url);
        assertThat(didOverrideUrl).isTrue();
        verify(htmlWebViewListener).onClicked();
        reset(htmlWebViewListener);

        stub(htmlWebView.wasClicked()).toReturn(false);
        didOverrideUrl = subject.shouldOverrideUrlLoading(htmlWebView, url);
        assertThat(Robolectric.getShadowApplication().getNextStartedActivity()).isNull();
        assertThat(didOverrideUrl).isTrue();
        verify(htmlWebViewListener, never()).onClicked();
        reset(htmlWebViewListener);
    }

    private Intent assertActivityStarted() {
        Intent startedActivity = Robolectric.getShadowApplication().getNextStartedActivity();
        assertThat(startedActivity).isNotNull();
        assertThat(startedActivity.getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK).isNotEqualTo(0);
        return startedActivity;
    }
}
