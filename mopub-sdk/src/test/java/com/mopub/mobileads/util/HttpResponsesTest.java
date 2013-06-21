package com.mopub.mobileads.util;

import com.mopub.mobileads.test.support.TestHttpResponseWithHeaders;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class HttpResponsesTest {

    private TestHttpResponseWithHeaders response;

    @Before
    public void setup() {
        response = new TestHttpResponseWithHeaders(200, "all is well");
    }

    @Test
    public void extractBooleanHeader_whenValueIsZero_shouldReturnFalse() throws Exception {
        response.addHeader("name", "0");

        assertThat(HttpResponses.extractBooleanHeader(response, "name")).isFalse();
    }

    @Test
    public void extractBooleanHeader_whenValueIsOne_shouldReturnTrue() throws Exception {
        response.addHeader("name", "1");

        assertThat(HttpResponses.extractBooleanHeader(response, "name")).isTrue();
    }

    @Test
    public void extractBooleanHeader_shouldDefaultToTrue() throws Exception {
        // no header added to response

        assertThat(HttpResponses.extractBooleanHeader(response, "name")).isTrue();
    }
}
