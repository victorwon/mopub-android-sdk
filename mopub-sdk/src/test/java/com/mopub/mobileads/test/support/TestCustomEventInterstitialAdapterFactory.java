/*
 * Copyright (c) 2010-2013, MoPub Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *  Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 *  Neither the name of 'MoPub Inc.' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.mopub.mobileads.test.support;

import com.mopub.mobileads.CustomEventInterstitialAdapter;
import com.mopub.mobileads.MoPubInterstitial;
import com.mopub.mobileads.factories.CustomEventInterstitialAdapterFactory;

import static org.mockito.Mockito.mock;

public class TestCustomEventInterstitialAdapterFactory extends CustomEventInterstitialAdapterFactory{
    private CustomEventInterstitialAdapter mockCustomEventInterstitalAdapter = mock(CustomEventInterstitialAdapter.class);
    private MoPubInterstitial latestMoPubInterstitial;
    private String latestClassName;
    private String latestClassData;

    public static CustomEventInterstitialAdapter getSingletonMock() {
        return getTestFactory().mockCustomEventInterstitalAdapter;
    }

    private static TestCustomEventInterstitialAdapterFactory getTestFactory() {
        return ((TestCustomEventInterstitialAdapterFactory)instance);
    }

    public static MoPubInterstitial getLatestMoPubInterstitial() {
        return getTestFactory().latestMoPubInterstitial;
    }

    public static String getLatestClassName() {
        return getTestFactory().latestClassName;
    }

    public static String getLatestClassData() {
        return getTestFactory().latestClassData;
    }

    @Override
    protected CustomEventInterstitialAdapter internalCreate(MoPubInterstitial moPubInterstitial, String className, String classData) {
        latestMoPubInterstitial = moPubInterstitial;
        latestClassName = className;
        latestClassData = classData;
        return mockCustomEventInterstitalAdapter;
    }
}
