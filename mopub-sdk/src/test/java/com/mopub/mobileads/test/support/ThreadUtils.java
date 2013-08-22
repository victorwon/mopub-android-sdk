package com.mopub.mobileads.test.support;

public class ThreadUtils {
    public static void pause(long millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie){
            // Ignore interrupts on this Thread.
        }
    }
}
