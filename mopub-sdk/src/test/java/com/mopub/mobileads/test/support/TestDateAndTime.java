package com.mopub.mobileads.test.support;

import com.mopub.mobileads.util.DateAndTime;

import java.util.Date;
import java.util.TimeZone;

public class TestDateAndTime extends DateAndTime {
    private TimeZone timeZone = TimeZone.getTimeZone("GMT-7");
    private Date now = new Date(1365553573L);

    public static TestDateAndTime getInstance() {
        return (TestDateAndTime) instance;
    }

    public void setNow(Date now) {
        this.now = now;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    @Override
    public TimeZone internalLocalTimeZone() {
        return timeZone;
    }

    @Override
    public Date internalNow() {
        return now;
    }
}
