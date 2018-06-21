package com.aerserv.admob;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.mediation.MediationAdRequest;
import com.google.android.gms.ads.mediation.customevent.CustomEventInterstitial;
import com.google.android.gms.ads.mediation.customevent.CustomEventInterstitialListener;
import com.aerserv.sdk.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


/**
 * Adapter to mediate AerServ interstitial ads through AdMob.  To use this adapter,
 * place the file in .../com/com.com.aerserv/admob of your source folder, and enter
 * com.com.com.aerserv.admob.AerServCustomEventInterstitial as Class Name when you create your custom event
 * on AdMob web interface.  Alternatively, you can place it in any folder of your choice and
 * change the package name to match your destination folder.  If you do so, please make sure
 * your custom event's Class Name value reflects this change.
 */
public class AerServCustomEventInterstitial implements CustomEventInterstitial {
    private static final String LOG_TAG = AerServCustomEventInterstitial.class.getSimpleName();

    private AerServInterstitial interstitial;

    @Override
    public void requestInterstitialAd(final Context context,
            final CustomEventInterstitialListener customEventInterstitialListener,
            String serverParameter, MediationAdRequest mediationAdRequest, Bundle bundle) {
        Log.d(LOG_TAG, "Requesting AerServ interstitial");

        // Verify context is of type Activity
        if (!(context instanceof Activity)) {
            Log.d(LOG_TAG, "AerServ SDK requires an Activity context to initialize");
            customEventInterstitialListener.onAdFailedToLoad(AdRequest.ERROR_CODE_INVALID_REQUEST);
            return;
        }

        AerServConfig config =
                AerServCustomEventUtils.getAerServConfig(context, serverParameter, bundle);
        if (config == null) {
            customEventInterstitialListener.onAdFailedToLoad(AdRequest.ERROR_CODE_INVALID_REQUEST);
            return;
        }
        config.setPreload(true);

        // Map AerServ event to AdMob events
        config.setEventListener(new AerServEventListener() {
            @Override
            public void onAerServEvent(final AerServEvent aerServEvent, List<Object> list) {
                Handler handler = new Handler(context.getMainLooper());
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        switch (aerServEvent) {
                            case PRELOAD_READY:
                                Log.d(LOG_TAG, "AerServ interstitial preloaded");
                                customEventInterstitialListener.onAdLoaded();
                                break;
                            case AD_FAILED:
                                Log.d(LOG_TAG, "AerServ interstitial failed to load");
                                customEventInterstitialListener
                                        .onAdFailedToLoad(AdRequest.ERROR_CODE_NO_FILL);
                                break;
                            case AD_CLICKED:
                                Log.d(LOG_TAG, "AerServ interstitial clicked");
                                customEventInterstitialListener.onAdClicked();
                                customEventInterstitialListener.onAdLeftApplication();
                                break;
                            case AD_IMPRESSION:
                                Log.d(LOG_TAG, "AerServ interstitial impression");
                                customEventInterstitialListener.onAdOpened();
                                break;
                            case AD_DISMISSED:
                                Log.d(LOG_TAG, "AerServ interstitial dismissed");
                                customEventInterstitialListener.onAdClosed();
                                break;
                            default:
                                break;
                        }
                    }
                };
                handler.post(runnable);
            }
        });

        // Request interstitial
        interstitial = new AerServInterstitial(config);
    }

    @Override
    public void showInterstitial() {
        if (interstitial != null) {
            interstitial.show();
        }
    }

    @Override
    public void onDestroy() {
        if (interstitial != null) {
            interstitial = null;
        }
    }

    @Override
    public void onPause() {
        if (interstitial != null) {
            interstitial.pause();
        }
    }

    @Override
    public void onResume() {
        if (interstitial != null) {
            interstitial.play();
        }
    }
}
