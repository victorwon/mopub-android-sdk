package com.mopub.mobileads.util;

import com.mopub.mobileads.test.support.SdkTestRunner;
import com.mopub.mobileads.test.support.TestHttpResponseWithHeaders;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(SdkTestRunner.class)
public class HttpResponsesTest {

    private TestHttpResponseWithHeaders response;

    @Before
    public void setup() {
        response = new TestHttpResponseWithHeaders(200, "all is well");
    }

    @Test
    public void extractBooleanHeader_whenValueIsZero_shouldReturnFalse() throws Exception {
        response.addHeader("name", "0");
        assertThat(HttpResponses.extractBooleanHeader(response, "name", false)).isFalse();

        response.addHeader("name", "0");
        assertThat(HttpResponses.extractBooleanHeader(response, "name", true)).isFalse();
    }

    @Test
    public void extractBooleanHeader_whenValueIsOne_shouldReturnTrue() throws Exception {
        response.addHeader("name", "1");
        assertThat(HttpResponses.extractBooleanHeader(response, "name", false)).isTrue();

        response.addHeader("name", "1");
        assertThat(HttpResponses.extractBooleanHeader(response, "name", true)).isTrue();
    }

    @Test
    public void extractBooleanHeader_shouldReturnDefaultValue() throws Exception {
        // no header added to response

        assertThat(HttpResponses.extractBooleanHeader(response, "name", false)).isFalse();
        assertThat(HttpResponses.extractBooleanHeader(response, "name", true)).isTrue();
    }
}
