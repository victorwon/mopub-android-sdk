package com.mopub.mobileads.util;

import org.apache.http.Header;
import org.apache.http.HttpResponse;

public class HttpResponses {
    public static String extractHeader(HttpResponse response, String headerName) {
        Header header = response.getFirstHeader(headerName);
        return header != null ? header.getValue() : null;
    }

    public static int extractIntHeader(HttpResponse response, String headerName) {
        String headerValue = extractHeader(response, headerName);
        return (headerValue != null) ? Integer.parseInt(headerValue.trim()) : 0;
    }

    public static boolean extractBooleanHeader(HttpResponse response, String headerName) {
        return !"0".equals(extractHeader(response, headerName));
    }
}
