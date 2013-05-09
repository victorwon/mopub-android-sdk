package com.mopub.mobileads.util;

import java.util.Date;
import java.util.TimeZone;

public class DateAndTime {
    protected static DateAndTime instance = new DateAndTime();

    public static void setInstance(DateAndTime newInstance) {
        instance = newInstance;
    }

    public static TimeZone localTimeZone() {
        return instance.internalLocalTimeZone();
    }

    public static Date now() {
        return instance.internalNow();
    }

    public TimeZone internalLocalTimeZone() {
        return TimeZone.getDefault();
    }

    public Date internalNow() {
        return new Date();
    }
}
