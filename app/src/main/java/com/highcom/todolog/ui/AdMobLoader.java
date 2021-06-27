package com.highcom.todolog.ui;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;
import android.widget.FrameLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.highcom.todolog.R;

public class AdMobLoader {
    private Activity mActivity;
    private FrameLayout mAdContainerView;
    private AdView mAdView;
    private String mUnitId;

    public AdMobLoader (Activity activity, FrameLayout adContainerView, String unitId) {
        mActivity = activity;
        mAdContainerView = adContainerView;
        mUnitId = unitId;
    }

    public void load() {
        mAdContainerView.post(() -> loadBanner());
    }

    public AdView getAdView() {
        return mAdView;
    }

    private void loadBanner() {
        // Create an ad request.
        mAdView = new AdView(mActivity);
        mAdView.setAdUnitId(mUnitId);
        mAdContainerView.removeAllViews();
        mAdContainerView.addView(mAdView);

        AdSize adSize = getAdSize();
        mAdView.setAdSize(adSize);

        AdRequest adRequest = new AdRequest.Builder().build();

        // Start loading the ad in the background.
        mAdView.loadAd(adRequest);
    }

    private AdSize getAdSize() {
        // Determine the screen width (less decorations) to use for the ad width.
        Display display = mActivity.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float density = outMetrics.density;

        float adWidthPixels = mAdContainerView.getWidth();

        // If the ad hasn't been laid out, default to the full screen width.
        if (adWidthPixels == 0) {
            adWidthPixels = outMetrics.widthPixels;
        }

        int adWidth = (int) (adWidthPixels / density);
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(mActivity, adWidth);
    }

}
