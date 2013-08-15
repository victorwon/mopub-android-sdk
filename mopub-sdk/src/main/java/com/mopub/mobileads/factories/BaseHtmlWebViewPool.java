package com.mopub.mobileads.factories;

import android.content.Context;
import com.mopub.mobileads.BaseHtmlWebView;

import java.util.LinkedList;
import java.util.Queue;

abstract public class BaseHtmlWebViewPool<V extends BaseHtmlWebView, L> {
    public final static int POOL_SIZE = 3;
    private Queue<V> mNextHtmlWebViews;
    protected Context mContext;

    BaseHtmlWebViewPool(Context context) {
        mContext = context;
        mNextHtmlWebViews = new LinkedList<V>();
        for (int i = 0; i < POOL_SIZE; i++) {
            mNextHtmlWebViews.add(createNewHtmlWebView());
        }
    }

    abstract protected V createNewHtmlWebView();
    abstract protected void initializeHtmlWebView(V htmlWebView, L customEventListener, boolean isScrollable, String redirectUrl, String clickthroughUrl);

    public V getNextHtmlWebView(
            L customEventListener,
            boolean isScrollable,
            String redirectUrl,
            String clickthroughUrl) {
        V returnValue = mNextHtmlWebViews.remove();

        mNextHtmlWebViews.add(createNewHtmlWebView());

        initializeHtmlWebView(returnValue, customEventListener, isScrollable, redirectUrl, clickthroughUrl);
        return returnValue;
    }

    void cleanup() {
        for (final V htmlBannerWebView : mNextHtmlWebViews) {
            htmlBannerWebView.destroy();
        }
        mNextHtmlWebViews.clear();
    }
}
