package com.mopub.mobileads;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.StateListDrawable;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.webkit.URLUtil;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.mopub.mobileads.MraidView.ExpansionStyle;
import com.mopub.mobileads.MraidView.NativeCloseButtonStyle;
import com.mopub.mobileads.MraidView.PlacementType;
import com.mopub.mobileads.MraidView.ViewState;

import java.util.ArrayList;

import static com.mopub.mobileads.resource.Drawables.INTERSTITIAL_CLOSE_BUTTON_NORMAL;
import static com.mopub.mobileads.resource.Drawables.INTERSTITIAL_CLOSE_BUTTON_PRESSED;

class MraidDisplayController extends MraidAbstractController {
    private static final String LOGTAG = "MraidDisplayController";
    private static final long VIEWABILITY_TIMER_MILLIS = 3000;
    private static final int CLOSE_BUTTON_SIZE_DP = 50;
    
    // The view's current state.
    private ViewState mViewState = ViewState.HIDDEN;
    
    // Tracks whether this controller's view responds to expand() calls.
    private final ExpansionStyle mExpansionStyle;

    // Tracks how this controller's view should display its native close button.
    private final NativeCloseButtonStyle mNativeCloseButtonStyle;

    // Separate instance of MraidView, for displaying "two-part" creatives via the expand(URL) API.
    private MraidView mTwoPartExpansionView;
    
    // A reference to the root view.
    private FrameLayout mRootView;
    
    // Tracks whether this controller's view is currently on-screen.
    private boolean mIsViewable;
    
    // Task that periodically checks whether this controller's view is on-screen.
    private Runnable mCheckViewabilityTask = new Runnable() {
        public void run() {
            boolean currentViewable = checkViewable();
            if (mIsViewable != currentViewable) {
                mIsViewable = currentViewable;
                getMraidView().fireChangeEventForProperty(
                        MraidViewableProperty.createWithViewable(mIsViewable));
            }
            mHandler.postDelayed(this, VIEWABILITY_TIMER_MILLIS);
        }
    };
    
    // Handler for scheduling viewability checks.
    private Handler mHandler = new Handler();
    
    // Stores the requested orientation for the Activity to which this controller's view belongs.
    // This is needed to restore the Activity's requested orientation in the event that the view
    // itself requires an orientation lock.
    private final int mOriginalRequestedOrientation;
    
    private BroadcastReceiver mOrientationBroadcastReceiver = new BroadcastReceiver() {
        private int mLastRotation;
        
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CONFIGURATION_CHANGED)) {
                int orientation = MraidDisplayController.this.getDisplayRotation();
                if (orientation != mLastRotation) {
                    mLastRotation = orientation;
                    MraidDisplayController.this.onOrientationChanged(mLastRotation);
                }
            }
        }
    };
    
    // Native close button, used for expanded content.
    private ImageView mCloseButton;
    
    // Tracks whether expanded content provides its own, non-native close button.
    private boolean mAdWantsCustomCloseButton;
    
    // The scale factor for a dip (relative to a 160 dpi screen).
    protected float mDensity;
    
    // The width of the screen in pixels.
    protected int mScreenWidth = -1;
    
    // The height of the screen in pixels.
    protected int mScreenHeight = -1;
    
    // The view's position within its parent.
    private int mViewIndexInParent;
    
    // A view that replaces the MraidView within its parent view when the MraidView is expanded
    // (i.e. moved to the top of the view hierarchy).
    private FrameLayout mPlaceholderView;
    private FrameLayout mAdContainerLayout;
    private RelativeLayout mExpansionLayout;

    MraidDisplayController(MraidView view, MraidView.ExpansionStyle expStyle, 
            MraidView.NativeCloseButtonStyle buttonStyle) {
        super(view);
        mExpansionStyle = expStyle;
        mNativeCloseButtonStyle = buttonStyle;
        
        Context context = getMraidView().getContext();
        mOriginalRequestedOrientation = (context instanceof Activity) ? 
                ((Activity) context).getRequestedOrientation() :
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;

        mAdContainerLayout = createAdContainerLayout();
        mExpansionLayout = createExpansionLayout();
        mPlaceholderView = createPlaceholderView();

        initialize();
    }
    
    private void initialize() {
        mViewState = ViewState.LOADING;
        initializeScreenMetrics();
        initializeViewabilityTimer();
        getMraidView().getContext().registerReceiver(mOrientationBroadcastReceiver,
                new IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED));
    }
    
    private void initializeScreenMetrics() {
        Context context = getMraidView().getContext();
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        mDensity = metrics.density;

        int statusBarHeight = 0, titleBarHeight = 0;
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            Window window = activity.getWindow();
            Rect rect = new Rect();
            window.getDecorView().getWindowVisibleDisplayFrame(rect);
            statusBarHeight = rect.top;
            int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
            titleBarHeight = contentViewTop - statusBarHeight;
        }
        
        int widthPixels = metrics.widthPixels;
        int heightPixels = metrics.heightPixels - statusBarHeight - titleBarHeight;
        mScreenWidth = (int) (widthPixels * (160.0 / metrics.densityDpi));
        mScreenHeight = (int) (heightPixels * (160.0 / metrics.densityDpi));
    }
    
    private void initializeViewabilityTimer() {
        mHandler.removeCallbacks(mCheckViewabilityTask);
        mHandler.post(mCheckViewabilityTask);
    }
    
    private int getDisplayRotation() {
        WindowManager wm = (WindowManager) getMraidView().getContext()
                .getSystemService(Context.WINDOW_SERVICE);
        return wm.getDefaultDisplay().getOrientation();
    }
    
    private void onOrientationChanged(int currentRotation) {
        initializeScreenMetrics();
        getMraidView().fireChangeEventForProperty(
                MraidScreenSizeProperty.createWithSize(mScreenWidth, mScreenHeight));
    }
    
    public void destroy() {
        mHandler.removeCallbacks(mCheckViewabilityTask);
        try {
            getMraidView().getContext().unregisterReceiver(mOrientationBroadcastReceiver);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Receiver not registered")) {
                // Ignore this exception.
            } else throw e;
        }
    }
    
    protected void initializeJavaScriptState() {
        ArrayList<MraidProperty> properties = new ArrayList<MraidProperty>();
        properties.add(MraidScreenSizeProperty.createWithSize(mScreenWidth, mScreenHeight));
        properties.add(MraidViewableProperty.createWithViewable(mIsViewable));
        getMraidView().fireChangeEventForProperties(properties);
        
        mViewState = ViewState.DEFAULT;
        getMraidView().fireChangeEventForProperty(MraidStateProperty.createWithViewState(mViewState));
    }
    
    protected boolean isExpanded() {
        return (mViewState == ViewState.EXPANDED);
    }
    
    protected void close() {
        if (mViewState == ViewState.EXPANDED) {
            resetViewToDefaultState();
            setOrientationLockEnabled(false);
            mViewState = ViewState.DEFAULT;
            getMraidView().fireChangeEventForProperty(MraidStateProperty.createWithViewState(mViewState));
        } else if (mViewState == ViewState.DEFAULT) {
            getMraidView().setVisibility(View.INVISIBLE);
            mViewState = ViewState.HIDDEN;
            getMraidView().fireChangeEventForProperty(MraidStateProperty.createWithViewState(mViewState));
        }
        
        if (getMraidView().getOnCloseListener() != null) {
            getMraidView().getOnCloseListener().onClose(getMraidView(), mViewState);
        }
    }
    
    private void resetViewToDefaultState() {
        setNativeCloseButtonEnabled(false);
        mAdContainerLayout.removeAllViewsInLayout();
        mExpansionLayout.removeAllViewsInLayout();
        mRootView.removeView(mExpansionLayout);

        getMraidView().requestLayout();
        
        ViewGroup parent = (ViewGroup) mPlaceholderView.getParent();
        parent.addView(getMraidView(), mViewIndexInParent);
        parent.removeView(mPlaceholderView);
        parent.invalidate();
    }
    
    protected void expand(String url, int width, int height, boolean shouldUseCustomClose,
            boolean shouldLockOrientation) {
        if (mExpansionStyle == MraidView.ExpansionStyle.DISABLED) return;
        
        if (url != null && !URLUtil.isValidUrl(url)) {
            getMraidView().fireErrorEvent("expand", "URL passed to expand() was invalid.");
            return;
        }

        // Obtain the root content view, since that's where we're going to insert the expanded 
        // content. We must do this before swapping the MraidView with its place-holder;
        // otherwise, getRootView() will return the wrong view (or null).
        mRootView = (FrameLayout) getMraidView().getRootView().findViewById(android.R.id.content);

        useCustomClose(shouldUseCustomClose);
        setOrientationLockEnabled(shouldLockOrientation);
        swapViewWithPlaceholderView();

        View expansionContentView = getMraidView();
        if (url != null) {
            mTwoPartExpansionView = new MraidView(getMraidView().getContext(), ExpansionStyle.DISABLED,
                    NativeCloseButtonStyle.AD_CONTROLLED, PlacementType.INLINE);
            mTwoPartExpansionView.setOnCloseListener(new MraidView.OnCloseListener() {
                public void onClose(MraidView view, ViewState newViewState) {
                    close();
                }
            });
            mTwoPartExpansionView.loadUrl(url);
            expansionContentView = mTwoPartExpansionView;
        }

        expandLayouts(expansionContentView, (int) (width * mDensity), (int) (height * mDensity));
        mRootView.addView(mExpansionLayout, new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT));
        
        if (mNativeCloseButtonStyle == MraidView.NativeCloseButtonStyle.ALWAYS_VISIBLE || 
                (!mAdWantsCustomCloseButton && 
                mNativeCloseButtonStyle != MraidView.NativeCloseButtonStyle.ALWAYS_HIDDEN)) {
            setNativeCloseButtonEnabled(true);
        }
        
        mViewState = ViewState.EXPANDED;
        getMraidView().fireChangeEventForProperty(MraidStateProperty.createWithViewState(mViewState));
        if (getMraidView().getOnExpandListener() != null) getMraidView().getOnExpandListener().onExpand(getMraidView());
    }
    
    private void swapViewWithPlaceholderView() {
        ViewGroup parent = (ViewGroup) getMraidView().getParent();
        if (parent == null) return;
        
        int index;
        int count = parent.getChildCount();
        for (index = 0; index < count; index++) {
            if (parent.getChildAt(index) == getMraidView()) break;
        }
        
        mViewIndexInParent = index;
        parent.addView(mPlaceholderView, index, 
                new ViewGroup.LayoutParams(getMraidView().getWidth(), getMraidView().getHeight()));
        parent.removeView(getMraidView());
    }

    private void expandLayouts(View expansionContentView, int expandWidth, int expandHeight) {
        int closeButtonSize = (int) (CLOSE_BUTTON_SIZE_DP * mDensity + 0.5f);
        if (expandWidth < closeButtonSize) expandWidth = closeButtonSize;
        if (expandHeight < closeButtonSize) expandHeight = closeButtonSize;

        View dimmingView = new View(getMraidView().getContext());
        dimmingView.setBackgroundColor(Color.TRANSPARENT);
        dimmingView.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        
        mExpansionLayout.addView(dimmingView, new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT));

        mAdContainerLayout.addView(expansionContentView, new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT));
        
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(expandWidth, expandHeight);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        mExpansionLayout.addView(mAdContainerLayout, lp);
    }

    private void setOrientationLockEnabled(boolean enabled) {
        Context context = getMraidView().getContext();
        Activity activity = null;
        try {
            activity = (Activity) context;
            int requestedOrientation = enabled ? 
                    activity.getResources().getConfiguration().orientation :
                    mOriginalRequestedOrientation;
            activity.setRequestedOrientation(requestedOrientation);
        } catch (ClassCastException e) {
            Log.d(LOGTAG, "Unable to modify device orientation.");
        }
    }
    
    protected void setNativeCloseButtonEnabled(boolean enabled) {
        if (mRootView == null) return;
        
        if (enabled) {
            if (mCloseButton == null) {
                StateListDrawable states = new StateListDrawable();
                states.addState(new int[] {-android.R.attr.state_pressed}, INTERSTITIAL_CLOSE_BUTTON_NORMAL.decodeImage(mRootView.getContext()));
                states.addState(new int[] {android.R.attr.state_pressed}, INTERSTITIAL_CLOSE_BUTTON_PRESSED.decodeImage(mRootView.getContext()));
                mCloseButton = new ImageButton(getMraidView().getContext());
                mCloseButton.setImageDrawable(states);
                mCloseButton.setBackgroundDrawable(null);
                mCloseButton.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        MraidDisplayController.this.close();
                    }
                });
            }
            
            int buttonSize = (int) (CLOSE_BUTTON_SIZE_DP * mDensity + 0.5f);
            FrameLayout.LayoutParams buttonLayout = new FrameLayout.LayoutParams(
                    buttonSize, buttonSize, Gravity.RIGHT);
            mAdContainerLayout.addView(mCloseButton, buttonLayout);
        } else {
            mAdContainerLayout.removeView(mCloseButton);
        }
        
        MraidView view = getMraidView();
        if (view.getOnCloseButtonStateChangeListener() != null) {
            view.getOnCloseButtonStateChangeListener().onCloseButtonStateChange(view, enabled);
        }
    }
    
    protected void useCustomClose(boolean shouldUseCustomCloseButton) {
        mAdWantsCustomCloseButton = shouldUseCustomCloseButton;
        
        MraidView view = getMraidView();
        boolean enabled = !shouldUseCustomCloseButton;
        if (view.getOnCloseButtonStateChangeListener() != null) {
            view.getOnCloseButtonStateChangeListener().onCloseButtonStateChange(view, enabled);
        }
    }
    
    protected boolean checkViewable() {
        // TODO: Perform more sophisticated check for viewable.
        return true;
    }

    FrameLayout createAdContainerLayout() {
        return new FrameLayout(getMraidView().getContext());
    }

    RelativeLayout createExpansionLayout() {
        return new RelativeLayout(getMraidView().getContext());
    }

    FrameLayout createPlaceholderView() {
        return new FrameLayout(getMraidView().getContext());
    }
}
