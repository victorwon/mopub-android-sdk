package com.mopub.mobileads;

import android.app.Activity;
import com.mopub.mobileads.test.support.SdkTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SdkTestRunner.class)
public class MraidViewTest {

    private MraidView subject;

    @Before
    public void setUp() throws Exception {
        subject = new MraidView(new Activity());
    }

    @Test
    public void loadHtmlData_whenDataIsNull_shouldNotBlowUp() throws Exception {
        subject.loadHtmlData(null);
        // pass
    }
}
