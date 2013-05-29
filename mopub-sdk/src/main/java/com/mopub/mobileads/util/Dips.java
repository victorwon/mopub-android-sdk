package com.mopub.mobileads.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;

public class Dips {
    public static float asFloatPixels(float dips, Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dips, displayMetrics);
    }

    public static int asIntPixels(float dips, Context context) {
        return (int) (asFloatPixels(dips, context) + 0.5f);
    }
}
