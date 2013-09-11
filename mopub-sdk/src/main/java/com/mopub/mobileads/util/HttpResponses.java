package com.mopub.mobileads.util;

import org.apache.http.Header;
import org.apache.http.HttpResponse;

import java.text.NumberFormat;
import java.util.Locale;

public class HttpResponses {
    public static String extractHeader(HttpResponse response, String headerName) {
        Header header = response.getFirstHeader(headerName);
        return header != null ? header.getValue() : null;
    }

    public static boolean extractBooleanHeader(HttpResponse response, String headerName, boolean defaultValue) {
        String header = extractHeader(response, headerName);
        if (header == null) {
            return defaultValue;
        }
        return header.equals("1");
    }

    public static Integer extractIntegerHeader(HttpResponse response, String headerName) {
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);
        numberFormat.setParseIntegerOnly(true);

        String headerValue = extractHeader(response, headerName);
        try {
            Number value = numberFormat.parse(headerValue.trim());
            return value.intValue();
        } catch (Exception e) {
            return null;
        }
    }

    public static int extractIntHeader(HttpResponse response, String headerName, int defaultValue) {
        Integer headerValue = extractIntegerHeader(response, headerName);
        if (headerValue == null) {
            return defaultValue;
        }

        return headerValue;
    }
}
