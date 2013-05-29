package com.mopub.mobileads.resource;

import android.app.Activity;
import com.mopub.mobileads.test.support.SdkTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(SdkTestRunner.class)
public class DrawablesTest {
    @Test
    public void decodeImage_shouldCacheDrawables() throws Exception {
        assertThat(Drawables.BACKGROUND.decodeImage(new Activity()))
                .isSameAs(Drawables.BACKGROUND.decodeImage(new Activity()));
    }
}
