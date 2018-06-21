package com.aerserv.admob;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.aerserv.sdk.AerServBanner;
import com.aerserv.sdk.AerServConfig;
import com.aerserv.sdk.AerServEvent;
import com.aerserv.sdk.AerServEventListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.mediation.MediationAdRequest;
import com.google.android.gms.ads.mediation.customevent.CustomEventBanner;
import com.google.android.gms.ads.mediation.customevent.CustomEventBannerListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


/**
 * Adapter to mediate AerServ banner ads through AdMob.  To use this adapter,
 * place the file in .../com/com.com.aerserv/admob of your source folder, and enter
 * com.com.com.aerserv.admob.AerServCustomEventBanner as Class Name when you create your custom event
 * on AdMob web interface.  Alternatively, you can place it in any folder of your choice and
 * change the package name to match your destination folder.  If you do so, please make sure
 * your custom event's Class Name value reflects this change.
 */
public class AerServCustomEventBanner implements CustomEventBanner {
    private static String LOG_TAG = AerServCustomEventBanner.class.getSimpleName();
    private AerServBanner banner;

    @Override
    public void requestBannerAd(final Context context,
            final CustomEventBannerListener customEventBannerListener, String serverParameter,
            final AdSize adSize, MediationAdRequest mediationAdRequest, Bundle bundle) {
        Log.d(LOG_TAG, "Requesting AerServ banner");

        // Verify context is of type Activity
        if (!(context instanceof Activity)) {
            Log.d(LOG_TAG, "AerServ SDK requires an Activity context to initialize");
            customEventBannerListener.onAdFailedToLoad(AdRequest.ERROR_CODE_INVALID_REQUEST);
            return;
        }

        AerServConfig config =
                AerServCustomEventUtils.getAerServConfig(context, serverParameter, bundle);
        if (config == null) {
            customEventBannerListener.onAdFailedToLoad(AdRequest.ERROR_CODE_INVALID_REQUEST);
            return;
        }
        config.setRefreshInterval(0);

        // Map AerServ event to AdMob events
        config.setEventListener(new AerServEventListener() {
            @Override
            public void onAerServEvent(final AerServEvent aerServEvent, List<Object> list) {
                Handler handler = new Handler(context.getMainLooper());
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        switch (aerServEvent) {
                            case AD_LOADED:
                                Log.d(LOG_TAG, "AerServ banner loaded");
                                customEventBannerListener.onAdLoaded(banner);
                                break;
                            case AD_FAILED:
                                Log.d(LOG_TAG, "AerServ banner failed to load");
                                customEventBannerListener
                                        .onAdFailedToLoad(AdRequest.ERROR_CODE_NO_FILL);
                                break;
                            case AD_CLICKED:
                                Log.d(LOG_TAG, "AerServ banner clicked");
                                customEventBannerListener.onAdClicked();
                                customEventBannerListener.onAdLeftApplication();
                                break;
                            case AD_IMPRESSION:
                                Log.d(LOG_TAG, "AerServ banner impression");
                                Log.d(LOG_TAG, "Setting AerServ banner view size to "
                                        + adSize.getWidthInPixels(context) + "x"
                                        + adSize.getHeightInPixels(context));
                                if (banner != null && banner.getLayoutParams() != null) {
                                    banner.getLayoutParams().width = adSize.getWidthInPixels(context);
                                    banner.getLayoutParams().height = adSize.getHeightInPixels(context);
                                }
                                break;
                            case AD_DISMISSED:
                                Log.d(LOG_TAG, "AerServ banner dismissed");
                                customEventBannerListener.onAdClosed();
                                break;
                            default:
                                break;
                        }
                    }
                };
                handler.post(runnable);
            }
        });

        banner = new AerServBanner(context);
        banner.configure(config).show();
    }

    @Override
    public void onDestroy() {
        if (banner != null) {
            banner.kill();
            banner = null;
        }
    }

    @Override
    public void onPause() {
        if (banner != null) {
            banner.pause();
        }
    }

    @Override
    public void onResume() {
        if (banner != null) {
            banner.play();
        }
    }
}
