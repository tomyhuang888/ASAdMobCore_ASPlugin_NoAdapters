package com.aerserv.admob;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.aerserv.sdk.AerServConfig;
import com.aerserv.sdk.AerServEvent;
import com.aerserv.sdk.AerServEventListener;
import com.aerserv.sdk.AerServInterstitial;
import com.aerserv.sdk.AerServSdk;
import com.aerserv.sdk.AerServVirtualCurrency;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.mediation.MediationAdRequest;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.mediation.MediationRewardedVideoAdAdapter;
import com.google.android.gms.ads.reward.mediation.MediationRewardedVideoAdListener;

import java.math.BigDecimal;
import java.util.List;


/**
 * Adapter to mediate AerServ VC-enabled interstitial ads through AdMob.  To use this adapter,
 * place the file in .../com/com.com.aerserv/admob of your source folder, and enter
 * com.com.com.aerserv.admob.AerServCustomEventRewardedInterstitial as Class Name when you create your
 * custom event on AdMob web interface.  Alternatively, you can place it in any folder of your
 * choice and change the package name to match your destination folder.  If you do so,
 * please make sure your custom event's Class Name value reflects this change.
 */
public class AerServCustomEventRewardedInterstitial implements MediationRewardedVideoAdAdapter {
    private static final String LOG_TAG = AerServCustomEventRewardedInterstitial.class.getSimpleName();

    private AerServInterstitial interstitial;
    private MediationRewardedVideoAdListener mediationRewardedVideoAdListener;
    private Context context;
    private static boolean isInitialized = false;

    @Override
    public void initialize(Context context, MediationAdRequest mediationAdRequest,
            String unused, final MediationRewardedVideoAdListener mediationRewardedVideoAdListener,
            Bundle serverParameters, Bundle mediationExtras) {
        Log.d(LOG_TAG, "Intializing AerServ SDK");

        this.context = context;
        this.mediationRewardedVideoAdListener = mediationRewardedVideoAdListener;

        // We assume that Publishers would call AerServSdk.init() in their app, so
        // here we just assume init call is ready.
        mediationRewardedVideoAdListener
                .onInitializationSucceeded(AerServCustomEventRewardedInterstitial.this);
        isInitialized = true;
    }

    @Override
    public void loadAd(MediationAdRequest mediationAdRequest, Bundle serverParameters,
            Bundle mediationExtras) {
        Log.d(LOG_TAG, "Requesting AerServ VC interstitial");

        // Read parameters and create AerServConfig based on their values
        String serverParamStr = serverParameters.getString(
                MediationRewardedVideoAdAdapter.CUSTOM_EVENT_SERVER_PARAMETER_FIELD);
        AerServConfig config = AerServCustomEventUtils.getAerServConfig(context, serverParamStr,
                mediationExtras);
        if (config == null) {
            mediationRewardedVideoAdListener.onAdFailedToLoad(this, AdRequest.ERROR_CODE_INVALID_REQUEST);
            return;
        }
        config.setPreload(true);

        final AerServCustomEventRewardedInterstitial self = this;
        config.setEventListener(new AerServEventListener() {
            @Override
            public void onAerServEvent(final AerServEvent aerServEvent, final List<Object> list) {
                Handler handler = new Handler(context.getMainLooper());
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        switch (aerServEvent) {
                            case PRELOAD_READY:
                                Log.d(LOG_TAG, "AerServ VC interstitial preloaded");
                                mediationRewardedVideoAdListener.onAdLoaded(self);
                                break;
                            case AD_IMPRESSION:
                                Log.d(LOG_TAG, "AerServ VC interstitial impression");
                                mediationRewardedVideoAdListener.onAdOpened(self);
                                break;
                            case VIDEO_START:
                                Log.d(LOG_TAG, "AerServ VC interstitial video start");
                                mediationRewardedVideoAdListener.onVideoStarted(self);
                                break;
                            case AD_DISMISSED:
                                Log.d(LOG_TAG, "AerServ VC interstitial dismissed");
                                mediationRewardedVideoAdListener.onAdClosed(self);
                                break;
                            case VC_REWARDED:
                                Log.d(LOG_TAG, "AerServ VC interstitial VC rewarded");

                                if (list.size() == 0) {
                                    break;
                                }
                                final AerServVirtualCurrency vc = (AerServVirtualCurrency) list.get(0);
                                if (vc == null) {
                                    break;
                                }

                                // AerServ uses BigDecimal, while AdMob uses int.  We need to
                                // round value to int, and then convert to int.  If value
                                // does not fit int, do not reward.
                                BigDecimal roundedAmount =
                                        vc.getAmount().setScale(0, BigDecimal.ROUND_HALF_UP);
                                final int amount;
                                try {
                                    amount = roundedAmount.intValueExact();
                                } catch (ArithmeticException e) {
                                    Log.i(LOG_TAG, "AerServ VC amount is too large to fit in "
                                            + "AdMob's integer.  No reward will be given.");
                                    break;
                                }

                                RewardItem rewardItem = new RewardItem() {
                                    @Override
                                    public String getType() {
                                        return vc.getName();
                                    }

                                    @Override
                                    public int getAmount() {
                                        return amount;
                                    }
                                };
                                mediationRewardedVideoAdListener.onRewarded(self, rewardItem);

                                break;
                            case AD_CLICKED:
                                Log.d(LOG_TAG, "AerServ VC interstitial clicked");
                                mediationRewardedVideoAdListener.onAdClicked(self);
                                mediationRewardedVideoAdListener.onAdLeftApplication(
                                        AerServCustomEventRewardedInterstitial.this);
                                break;
                            case AD_FAILED:
                                Log.d(LOG_TAG, "AerServ VC interstitial failed to load");
                                mediationRewardedVideoAdListener
                                        .onAdFailedToLoad(self,AdRequest.ERROR_CODE_NO_FILL);
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
    public void showVideo() {
        if (interstitial != null) {
            interstitial.show();
        }
    }

    @Override
    public boolean isInitialized() {
        return isInitialized;
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
