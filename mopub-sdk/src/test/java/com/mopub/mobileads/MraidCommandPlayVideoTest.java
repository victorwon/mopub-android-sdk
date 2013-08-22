package com.mopub.mobileads;

import android.app.Activity;
import com.mopub.mobileads.test.support.SdkTestRunner;
import com.mopub.mobileads.test.support.TestMraidViewFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

import static com.mopub.mobileads.MraidCommandPlayVideo.URI_KEY;
import static com.mopub.mobileads.MraidVideoPlayerActivityTest.assertVideoPlayerActivityStarted;
import static org.mockito.Mockito.stub;

@RunWith(SdkTestRunner.class)
public class MraidCommandPlayVideoTest {
    public static final String EXPECTED_URI = "http://expected.uri";

    private MraidCommandPlayVideo subject;
    private MraidView mraidView;

    @Before
    public void setup() {
        Map<String, String> params = new HashMap<String, String>();

        params.put(URI_KEY, EXPECTED_URI);

        mraidView = TestMraidViewFactory.getSingletonMock();
        stub(mraidView.getContext()).toReturn(new Activity());
        MraidDisplayController displayController = new MraidDisplayController(mraidView, null, null);
        stub(mraidView.getDisplayController()).toReturn(displayController);
        subject = new MraidCommandPlayVideo(params, mraidView);
    }

    @Test
    public void execute_shouldPlayVideo() throws Exception {
        subject.execute();

        assertVideoPlayerActivityStarted(EXPECTED_URI);
    }
}
