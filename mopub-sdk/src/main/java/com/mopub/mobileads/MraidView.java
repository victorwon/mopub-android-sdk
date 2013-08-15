package com.mopub.mobileads;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.mopub.mobileads.resource.MraidJavascript;
import com.mopub.mobileads.util.Strings;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MraidView extends BaseWebView {
    private static final String LOGTAG = "MraidView";
    
    private MraidBrowserController mBrowserController;
    private MraidDisplayController mDisplayController;
    
    private WebViewClient mWebViewClient;
    private WebChromeClient mWebChromeClient;
    
    private boolean mHasFiredReadyEvent;
    private final PlacementType mPlacementType;
    
    static class MraidListenerInfo {
        private OnExpandListener mOnExpandListener;
        private OnCloseListener mOnCloseListener;
        private OnReadyListener mOnReadyListener;
        private OnFailureListener mOnFailureListener;
        private OnCloseButtonStateChangeListener mOnCloseButtonListener;
        private OnOpenListener mOnOpenListener;
    }
    private MraidListenerInfo mListenerInfo;
    
    public enum ViewState {
        LOADING,
        DEFAULT,
        EXPANDED,
        HIDDEN
    }

    public enum ExpansionStyle {
        ENABLED,
        DISABLED
    }

    public enum NativeCloseButtonStyle {
        ALWAYS_VISIBLE,
        ALWAYS_HIDDEN,
        AD_CONTROLLED
    }
    
    public enum PlacementType {
        INLINE,
        INTERSTITIAL
    }

    public MraidView(Context context) {
        this(context, ExpansionStyle.ENABLED, NativeCloseButtonStyle.AD_CONTROLLED,
                PlacementType.INLINE);
    }

    public MraidView(Context context, ExpansionStyle expStyle, NativeCloseButtonStyle buttonStyle,
                     PlacementType placementType) {
        super(context);
        mPlacementType = placementType;
        initialize(expStyle, buttonStyle);
    }
    
    private void initialize(ExpansionStyle expStyle, NativeCloseButtonStyle buttonStyle) {
        setScrollContainer(false);
        setBackgroundColor(Color.TRANSPARENT);
        
        setVerticalScrollBarEnabled(false);
        setHorizontalScrollBarEnabled(false);
        
        setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_UP:
                        if (!v.hasFocus()) {
                            v.requestFocus();
                        }
                        break;
                }
                return false;
            }
        });
        
        getSettings().setJavaScriptEnabled(true);
        
        mBrowserController = new MraidBrowserController(this);
        mDisplayController = new MraidDisplayController(this, expStyle, buttonStyle);
        
        mWebViewClient = new MraidWebViewClient();
        setWebViewClient(mWebViewClient);
        
        mWebChromeClient = new MraidWebChromeClient();
        setWebChromeClient(mWebChromeClient);
        
        mListenerInfo = new MraidListenerInfo();
    }
    
    public void destroy() {
        mDisplayController.destroy();
        super.destroy();
    }

    public void loadHtmlData(String data) {
        if (data == null) return;

        // If the string data lacks the HTML boilerplate, add it.
        if (data.indexOf("<html>") == -1) {
            data = "<html><head></head><body style='margin:0;padding:0;'>" + data + 
                    "</body></html>";
        }
        
        // Inject the MRAID JavaScript bridge.
        data = data.replace("<head>", "<head><script>" + MraidJavascript.JAVASCRIPT_SOURCE + "</script>");

        loadDataWithBaseURL(null, data, "text/html", "UTF-8", null);
    }

    public void loadUrl(String url) {
        HttpClient httpClient = new DefaultHttpClient();
        String outString = "";
        
        try {
            HttpGet httpGet = new HttpGet(url);
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
    
            if (entity != null) {
                outString = Strings.fromStream(entity.getContent());
            }
        } catch (IllegalArgumentException e) {
            Log.d("MoPub", "Mraid loadUrl failed (IllegalArgumentException): "+url);
            notifyOnFailureListener();
            return;
        } catch (ClientProtocolException e) {
            notifyOnFailureListener();
            return;
        } catch (IOException e) {
            notifyOnFailureListener();
            return;
        }

        loadHtmlData(outString);
    }
    
    private void notifyOnFailureListener() {
        if (mListenerInfo.mOnFailureListener != null) {
            mListenerInfo.mOnFailureListener.onFailure(this);
        }
    }

    // Controllers /////////////////////////////////////////////////////////////////////////////////
    
    protected MraidBrowserController getBrowserController() {
        return mBrowserController;
    }
    
    protected MraidDisplayController getDisplayController() {
        return mDisplayController;
    }
    
    // Listeners ///////////////////////////////////////////////////////////////////////////////////
    
    public void setOnExpandListener(OnExpandListener listener) {
        mListenerInfo.mOnExpandListener = listener;
    }
    
    public OnExpandListener getOnExpandListener() {
        return mListenerInfo.mOnExpandListener;
    }
    
    public void setOnCloseListener(OnCloseListener listener) {
        mListenerInfo.mOnCloseListener = listener;
    }
    
    public OnCloseListener getOnCloseListener() {
        return mListenerInfo.mOnCloseListener;
    }
    
    public void setOnReadyListener(OnReadyListener listener) {
        mListenerInfo.mOnReadyListener = listener;
    }
    
    public OnReadyListener getOnReadyListener() {
        return mListenerInfo.mOnReadyListener;
    }
    
    public void setOnFailureListener(OnFailureListener listener) {
        mListenerInfo.mOnFailureListener = listener;
    }
    
    public OnFailureListener getOnFailureListener() {
        return mListenerInfo.mOnFailureListener;
    }
    
    public void setOnCloseButtonStateChange(OnCloseButtonStateChangeListener listener) {
        mListenerInfo.mOnCloseButtonListener = listener;
    }
    
    public OnCloseButtonStateChangeListener getOnCloseButtonStateChangeListener() {
        return mListenerInfo.mOnCloseButtonListener;
    }
    
    public void setOnOpenListener(OnOpenListener listener) {
        mListenerInfo.mOnOpenListener = listener;
    }
    
    public OnOpenListener getOnOpenListener() {
        return mListenerInfo.mOnOpenListener;
    }
    
    // JavaScript injection ////////////////////////////////////////////////////////////////////////
    
    protected void injectJavaScript(String js) {
        if (js != null) super.loadUrl("javascript:" + js);
    }
    
    protected void fireChangeEventForProperty(MraidProperty property) {
        String json = "{" + property.toString() + "}";
        injectJavaScript("window.mraidbridge.fireChangeEvent(" + json + ");");
        Log.d(LOGTAG, "Fire change: " + json);
    }
    
    protected void fireChangeEventForProperties(ArrayList<MraidProperty> properties) {
        String props = properties.toString();
        if (props.length() < 2) return;
        
        String json = "{" + props.substring(1, props.length() - 1) + "}";
        injectJavaScript("window.mraidbridge.fireChangeEvent(" + json + ");");
        Log.d(LOGTAG, "Fire changes: " + json);
    }
    
    protected void fireErrorEvent(String action, String message) {
        injectJavaScript("window.mraidbridge.fireErrorEvent('" + action + "', '" + message + "');");
    }
    
    protected void fireReadyEvent() {
        injectJavaScript("window.mraidbridge.fireReadyEvent();");
    }
    
    protected void fireNativeCommandCompleteEvent(String command) {
        injectJavaScript("window.mraidbridge.nativeCallComplete('" + command + "');");
    }
    
    private boolean tryCommand(URI uri) {
        String commandType = uri.getHost();
        List<NameValuePair> list = URLEncodedUtils.parse(uri, "UTF-8");
        Map<String, String> params = new HashMap<String, String>();
        for (NameValuePair pair : list) {
            params.put(pair.getName(), pair.getValue());
        }
        
        MraidCommand command = MraidCommandRegistry.createCommand(commandType, params, this);
        if (command == null) {
            fireNativeCommandCompleteEvent(commandType);
            return false;
        } else {
            command.execute();
            fireNativeCommandCompleteEvent(commandType);
            return true;
        }
    }

    private class MraidWebViewClient extends WebViewClient {
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, 
                String failingUrl) {
            Log.d(LOGTAG, "Error: " + description);
            super.onReceivedError(view, errorCode, description, failingUrl);
        }
        
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Uri uri = Uri.parse(url);
            String scheme = uri.getScheme();
            
            if (scheme.equals("mopub")) return true;
            if (scheme.equals("mraid")) {
                tryCommand(URI.create(url)); // java.net.URI, not android.net.Uri
                return true;
            }
            
            Intent i = new Intent();
            i.setAction(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            
            try {
                getContext().startActivity(i);
                return true;
            } catch (ActivityNotFoundException e) {
                return false;
            }
        }
        
        @Override
        public void onPageFinished(WebView view, String url) {
            if (!mHasFiredReadyEvent) {
                mDisplayController.initializeJavaScriptState();
                fireChangeEventForProperty(
                        MraidPlacementTypeProperty.createWithType(mPlacementType));
                fireReadyEvent();
                if (getOnReadyListener() != null) getOnReadyListener().onReady(MraidView.this);
                mHasFiredReadyEvent = true;
            }
        }
        
        @Override
        public void onLoadResource(WebView view, String url) {
            Log.d(LOGTAG, "Loaded resource: " + url);
        }
    }
    
    private class MraidWebChromeClient extends WebChromeClient {
        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            Log.d(LOGTAG, message);
            return false;
        }
    }
    
    public interface OnExpandListener {
        public void onExpand(MraidView view);
    }
    
    public interface OnCloseListener {
        public void onClose(MraidView view, ViewState newViewState);
    }
    
    public interface OnReadyListener {
        public void onReady(MraidView view);
    }
    
    public interface OnFailureListener {
        public void onFailure(MraidView view);
    }
    
    public interface OnCloseButtonStateChangeListener {
        public void onCloseButtonStateChange(MraidView view, boolean enabled);
    }
    
    public interface OnOpenListener {
        public void onOpen(MraidView view);
    }
}
