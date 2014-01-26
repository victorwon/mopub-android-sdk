package com.mopub.mobileads.util.vast;

import com.mopub.mobileads.test.support.SdkTestRunner;
import com.mopub.mobileads.test.support.ThreadUtils;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.robolectric.Robolectric;
import org.robolectric.tester.org.apache.http.FakeHttpLayer;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(SdkTestRunner.class)
public class VastManagerTest {
    private static final String TEST_VAST_XML_STRING = "<VAST version='2.0'><Ad id='62833'><Wrapper><AdSystem>Tapad</AdSystem><VASTAdTagURI>http://dsp.x-team.staging.mopub.com/xml</VASTAdTagURI><Impression>http://myTrackingURL/wrapper/impression1</Impression><Impression>http://myTrackingURL/wrapper/impression2</Impression><Creatives><Creative AdID='62833'><Linear><TrackingEvents><Tracking event='creativeView'>http://myTrackingURL/wrapper/creativeView</Tracking><Tracking event='start'>http://myTrackingURL/wrapper/start</Tracking><Tracking event='midpoint'>http://myTrackingURL/wrapper/midpoint</Tracking><Tracking event='firstQuartile'>http://myTrackingURL/wrapper/firstQuartile</Tracking><Tracking event='thirdQuartile'>http://myTrackingURL/wrapper/thirdQuartile</Tracking><Tracking event='complete'>http://myTrackingURL/wrapper/complete</Tracking><Tracking event='mute'>http://myTrackingURL/wrapper/mute</Tracking><Tracking event='unmute'>http://myTrackingURL/wrapper/unmute</Tracking><Tracking event='pause'>http://myTrackingURL/wrapper/pause</Tracking><Tracking event='resume'>http://myTrackingURL/wrapper/resume</Tracking><Tracking event='fullscreen'>http://myTrackingURL/wrapper/fullscreen</Tracking></TrackingEvents><VideoClicks><ClickTracking>http://myTrackingURL/wrapper/click</ClickTracking></VideoClicks></Linear></Creative></Creatives></Wrapper></Ad></VAST><MP_TRACKING_URLS><MP_TRACKING_URL>http://www.mopub.com/imp1</MP_TRACKING_URL><MP_TRACKING_URL>http://www.mopub.com/imp2</MP_TRACKING_URL></MP_TRACKING_URLS>";
    private static final String TEST_NESTED_VAST_XML_STRING = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><VAST version='2.0'><Ad id='57722'><InLine><AdSystem version='1.0'>Tapad</AdSystem><AdTitle><![CDATA[PKW6T_LIV_DSN_Audience_TAPAD_3rd Party Audience Targeting_Action Movi]]></AdTitle><Description/><Impression><![CDATA[http://rtb-test.dev.tapad.com:8080/creative/imp.png?ts=1374099035457&svid=1&creative_id=30731&ctx_type=InApp&ta_pinfo=JnRhX2JpZD1iNDczNTQwMS1lZjJkLTExZTItYTNkNS0yMjAwMGE4YzEwOWQmaXA9OTguMTE2LjEyLjk0JnNzcD1MSVZFUkFJTCZ0YV9iaWRkZXJfaWQ9NTEzJTNBMzA1NSZjdHg9MTMzMSZ0YV9jYW1wYWlnbl9pZD01MTMmZGM9MTAwMjAwMzAyOSZ1YT1Nb3ppbGxhJTJGNS4wKyUyOE1hY2ludG9zaCUzQitJbnRlbCtNYWMrT1MrWCsxMF84XzMlMjkrQXBwbGVXZWJLaXQlMkY1MzcuMzYrJTI4S0hUTUwlMkMrbGlrZStHZWNrbyUyOStDaHJvbWUlMkYyNy4wLjE0NTMuMTE2K1NhZmFyaSUyRjUzNy4zNiZjcHQ9VkFTVCZkaWQ9ZDgyNWZjZDZlNzM0YTQ3ZTE0NWM4ZTkyNzMwMjYwNDY3YjY1NjllMSZpZD1iNDczNTQwMC1lZjJkLTExZTItYTNkNS0yMjAwMGE4YzEwOWQmcGlkPUNPTVBVVEVSJnN2aWQ9MSZicD0zNS4wMCZjdHhfdHlwZT1BJnRpZD0zMDU1JmNyaWQ9MzA3MzE%3D&liverail_cp=1]]></Impression><Creatives><Creative sequence='1' id='57722'><Linear><Duration>00:00:15</Duration><VideoClicks><ClickThrough><![CDATA[http://rtb-test.dev.tapad.com:8080/click?ta_pinfo=JnRhX2JpZD1iNDczNTQwMS1lZjJkLTExZTItYTNkNS0yMjAwMGE4YzEwOWQmaXA9OTguMTE2LjEyLjk0JnNzcD1MSVZFUkFJTCZ0YV9iaWRkZXJfaWQ9NTEzJTNBMzA1NSZjdHg9MTMzMSZ0YV9jYW1wYWlnbl9pZD01MTMmZGM9MTAwMjAwMzAyOSZ1YT1Nb3ppbGxhJTJGNS4wKyUyOE1hY2ludG9zaCUzQitJbnRlbCtNYWMrT1MrWCsxMF84XzMlMjkrQXBwbGVXZWJLaXQlMkY1MzcuMzYrJTI4S0hUTUwlMkMrbGlrZStHZWNrbyUyOStDaHJvbWUlMkYyNy4wLjE0NTMuMTE2K1NhZmFyaSUyRjUzNy4zNiZjcHQ9VkFTVCZkaWQ9ZDgyNWZjZDZlNzM0YTQ3ZTE0NWM4ZTkyNzMwMjYwNDY3YjY1NjllMSZpZD1iNDczNTQwMC1lZjJkLTExZTItYTNkNS0yMjAwMGE4YzEwOWQmcGlkPUNPTVBVVEVSJnN2aWQ9MSZicD0zNS4wMCZjdHhfdHlwZT1BJnRpZD0zMDU1JmNyaWQ9MzA3MzE%3D&crid=30731&ta_action_id=click&ts=1374099035458&redirect=http%3A%2F%2Ftapad.com]]></ClickThrough></VideoClicks><MediaFiles><MediaFile delivery='progressive' bitrate='416' width='480' height='360' type='video/mp4'><![CDATA[https://s3.amazonaws.com/mopub-vast/tapad-video.mp4]]></MediaFile></MediaFiles></Linear></Creative></Creatives></InLine></Ad></VAST>";

    private VastManager mVastManager;
    private FakeHttpLayer mFakeHttpLayer;
    private boolean mIsListenerNotified;

    @Before
    public void setup() {
        mVastManager = new VastManager();
        mFakeHttpLayer = Robolectric.getFakeHttpLayer();
        mIsListenerNotified = false;
    }

    private void processVast() {
        mVastManager.processVast(TEST_VAST_XML_STRING, new VastManager.VastManagerListener() {
            @Override
            public void onComplete(VastManager vastManager) {
                mIsListenerNotified = true;
            }
        });

        Robolectric.runBackgroundTasks();
        ThreadUtils.pause(10);
        Robolectric.runUiThreadTasks();
    }

    @Test
    public void processVast_shouldNotifyTheListenerAndContainTheCorrectVastValues() {
        mFakeHttpLayer.addPendingHttpResponse(200, TEST_NESTED_VAST_XML_STRING);

        processVast();

        assertThat(mIsListenerNotified).isEqualTo(true);
        assertThat(mVastManager.getMediaFileUrl()).isEqualTo("https://s3.amazonaws.com/mopub-vast/tapad-video.mp4");
        assertThat(mVastManager.getClickThroughUrl()).isEqualTo("http://rtb-test.dev.tapad.com:8080/click?ta_pinfo=JnRhX2JpZD1iNDczNTQwMS1lZjJkLTExZTItYTNkNS0yMjAwMGE4YzEwOWQmaXA9OTguMTE2LjEyLjk0JnNzcD1MSVZFUkFJTCZ0YV9iaWRkZXJfaWQ9NTEzJTNBMzA1NSZjdHg9MTMzMSZ0YV9jYW1wYWlnbl9pZD01MTMmZGM9MTAwMjAwMzAyOSZ1YT1Nb3ppbGxhJTJGNS4wKyUyOE1hY2ludG9zaCUzQitJbnRlbCtNYWMrT1MrWCsxMF84XzMlMjkrQXBwbGVXZWJLaXQlMkY1MzcuMzYrJTI4S0hUTUwlMkMrbGlrZStHZWNrbyUyOStDaHJvbWUlMkYyNy4wLjE0NTMuMTE2K1NhZmFyaSUyRjUzNy4zNiZjcHQ9VkFTVCZkaWQ9ZDgyNWZjZDZlNzM0YTQ3ZTE0NWM4ZTkyNzMwMjYwNDY3YjY1NjllMSZpZD1iNDczNTQwMC1lZjJkLTExZTItYTNkNS0yMjAwMGE4YzEwOWQmcGlkPUNPTVBVVEVSJnN2aWQ9MSZicD0zNS4wMCZjdHhfdHlwZT1BJnRpZD0zMDU1JmNyaWQ9MzA3MzE%3D&crid=30731&ta_action_id=click&ts=1374099035458&redirect=http%3A%2F%2Ftapad.com");
        assertThat(mVastManager.getImpressionTrackers().size()).isEqualTo(5);
        assertThat(mVastManager.getVideoStartTrackers().size()).isEqualTo(1);
        assertThat(mVastManager.getVideoFirstQuartileTrackers().size()).isEqualTo(1);
        assertThat(mVastManager.getVideoMidpointTrackers().size()).isEqualTo(1);
        assertThat(mVastManager.getVideoThirdQuartileTrackers().size()).isEqualTo(1);
        assertThat(mVastManager.getVideoCompleteTrackers().size()).isEqualTo(1);
        assertThat(mVastManager.getClickTrackers().size()).isEqualTo(1);
    }

    @Test
    public void processVast_shouldNotifyTheListenerAndContainTheCorrectVastValuesWhenAVastRedirectFails() {
        mFakeHttpLayer.addPendingHttpResponse(404, "");

        processVast();

        assertThat(mIsListenerNotified).isEqualTo(true);
        assertThat(mVastManager.getMediaFileUrl()).isEqualTo(null);
        assertThat(mVastManager.getClickThroughUrl()).isEqualTo(null);
        assertThat(mVastManager.getImpressionTrackers().size()).isEqualTo(4);
        assertThat(mVastManager.getVideoFirstQuartileTrackers().size()).isEqualTo(1);
    }

    @Test
    public void processVast_shouldNotFollowRedirectsOnceTheLimitHasBeenReached() {
        mFakeHttpLayer.addPendingHttpResponse(200, TEST_NESTED_VAST_XML_STRING);

        mVastManager.setTimesFollowedVastRedirect(VastManager.MAX_TIMES_TO_FOLLOW_VAST_REDIRECT);

        processVast();

        assertThat(mIsListenerNotified).isEqualTo(true);
        assertThat(mVastManager.getMediaFileUrl()).isEqualTo(null);
        assertThat(mVastManager.getClickThroughUrl()).isEqualTo(null);
        assertThat(mVastManager.getImpressionTrackers().size()).isEqualTo(4);
        assertThat(mVastManager.getVideoFirstQuartileTrackers().size()).isEqualTo(1);
    }

    @Test
    public void processVast_shouldHandleNullVastXmlGracefully() {
        mVastManager.processVast(null, new VastManager.VastManagerListener() {
            @Override
            public void onComplete(VastManager vastManager) {
                mIsListenerNotified = true;
            }
        });

        Robolectric.runBackgroundTasks();
        ThreadUtils.pause(10);
        Robolectric.runUiThreadTasks();

        assertThat(mIsListenerNotified).isEqualTo(true);
        assertThat(mVastManager.getMediaFileUrl()).isEqualTo(null);
        assertThat(mVastManager.getClickThroughUrl()).isEqualTo(null);
        assertThat(mVastManager.getImpressionTrackers().size()).isEqualTo(0);
        assertThat(mVastManager.getVideoFirstQuartileTrackers().size()).isEqualTo(0);
    }

    @Test
    public void processVast_shouldHandleEmptyVastXmlGracefully() {
        mVastManager.processVast("", new VastManager.VastManagerListener() {
            @Override
            public void onComplete(VastManager vastManager) {
                mIsListenerNotified = true;
            }
        });

        Robolectric.runBackgroundTasks();
        ThreadUtils.pause(10);
        Robolectric.runUiThreadTasks();

        assertThat(mIsListenerNotified).isEqualTo(true);
        assertThat(mVastManager.getMediaFileUrl()).isEqualTo(null);
        assertThat(mVastManager.getClickThroughUrl()).isEqualTo(null);
        assertThat(mVastManager.getImpressionTrackers().size()).isEqualTo(0);
        assertThat(mVastManager.getVideoFirstQuartileTrackers().size()).isEqualTo(0);
    }

    @Test
    public void cancel_shouldCancelBackgroundProcessingAndNotNotifyTheListener() {
        mFakeHttpLayer.addPendingHttpResponse(200, TEST_NESTED_VAST_XML_STRING);

        Robolectric.getBackgroundScheduler().pause();

        mVastManager.processVast(TEST_VAST_XML_STRING, new VastManager.VastManagerListener() {
            @Override
            public void onComplete(VastManager vastManager) {
                mIsListenerNotified = true;
            }
        });

        mVastManager.cancel();

        Robolectric.runBackgroundTasks();
        ThreadUtils.pause(10);
        Robolectric.runUiThreadTasks();

        assertThat(mIsListenerNotified).isEqualTo(false);
        assertThat(mVastManager.getMediaFileUrl()).isEqualTo(null);
        assertThat(mVastManager.getClickThroughUrl()).isEqualTo(null);
        assertThat(mVastManager.getImpressionTrackers().size()).isEqualTo(0);
        assertThat(mVastManager.getVideoFirstQuartileTrackers().size()).isEqualTo(0);
    }

    @Test
    public void processVast_shouldHandleMultipleRedirects() {
        mFakeHttpLayer.addPendingHttpResponse(200, TEST_VAST_XML_STRING);
        mFakeHttpLayer.addPendingHttpResponse(200, TEST_VAST_XML_STRING);
        mFakeHttpLayer.addPendingHttpResponse(200, TEST_NESTED_VAST_XML_STRING);

        processVast();

        // at this point it should have 3 sets of data from TEST_VAST_XML_STRING and one set from TEST_NESTED_VAST_XML_STRING
        assertThat(mIsListenerNotified).isEqualTo(true);
        assertThat(mVastManager.getMediaFileUrl()).isEqualTo("https://s3.amazonaws.com/mopub-vast/tapad-video.mp4");
        assertThat(mVastManager.getClickThroughUrl()).isEqualTo("http://rtb-test.dev.tapad.com:8080/click?ta_pinfo=JnRhX2JpZD1iNDczNTQwMS1lZjJkLTExZTItYTNkNS0yMjAwMGE4YzEwOWQmaXA9OTguMTE2LjEyLjk0JnNzcD1MSVZFUkFJTCZ0YV9iaWRkZXJfaWQ9NTEzJTNBMzA1NSZjdHg9MTMzMSZ0YV9jYW1wYWlnbl9pZD01MTMmZGM9MTAwMjAwMzAyOSZ1YT1Nb3ppbGxhJTJGNS4wKyUyOE1hY2ludG9zaCUzQitJbnRlbCtNYWMrT1MrWCsxMF84XzMlMjkrQXBwbGVXZWJLaXQlMkY1MzcuMzYrJTI4S0hUTUwlMkMrbGlrZStHZWNrbyUyOStDaHJvbWUlMkYyNy4wLjE0NTMuMTE2K1NhZmFyaSUyRjUzNy4zNiZjcHQ9VkFTVCZkaWQ9ZDgyNWZjZDZlNzM0YTQ3ZTE0NWM4ZTkyNzMwMjYwNDY3YjY1NjllMSZpZD1iNDczNTQwMC1lZjJkLTExZTItYTNkNS0yMjAwMGE4YzEwOWQmcGlkPUNPTVBVVEVSJnN2aWQ9MSZicD0zNS4wMCZjdHhfdHlwZT1BJnRpZD0zMDU1JmNyaWQ9MzA3MzE%3D&crid=30731&ta_action_id=click&ts=1374099035458&redirect=http%3A%2F%2Ftapad.com");
        assertThat(mVastManager.getImpressionTrackers().size()).isEqualTo(13);
        assertThat(mVastManager.getVideoStartTrackers().size()).isEqualTo(3);
        assertThat(mVastManager.getVideoFirstQuartileTrackers().size()).isEqualTo(3);
        assertThat(mVastManager.getVideoMidpointTrackers().size()).isEqualTo(3);
        assertThat(mVastManager.getVideoThirdQuartileTrackers().size()).isEqualTo(3);
        assertThat(mVastManager.getVideoCompleteTrackers().size()).isEqualTo(3);
        assertThat(mVastManager.getClickTrackers().size()).isEqualTo(3);
    }
}
