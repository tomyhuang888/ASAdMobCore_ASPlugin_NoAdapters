package com.aerserv.admobcore_asplugin_noadapters;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;


import com.aerserv.sdk.AerServSdk;
import com.example.thomash.aerserv.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdListener;

import com.google.android.gms.ads.InterstitialAd;

import static com.aerserv.sdk.utils.WebViewJSRunner.LOG_TAG;

public class MainActivity extends AppCompatActivity {
    private AdView mAdMobBanner;
    private InterstitialAd mAdMobInterstitial;

    RelativeLayout container;


    private void initVar(){
        mAdMobInterstitial = new InterstitialAd(this);
        container = (RelativeLayout) findViewById(R.id.bannerView);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
        // My AdMob app ID: ca-app-pub-8745611599033887~1432285807
        // iBugs: ca-app-pub-2877795938017911~6340221382
//        MobileAds.initialize(this, "ca-app-pub-2877795938017911~6340221382");
        AerServSdk.init(MainActivity.this, "380000");
        initVar();


        //banner
//        String testAdid = "ca-app-pub-3940256099942544/6300978111";

//        String bannerAdid = "ca-app-pub-8745611599033887/7231407393";
//        String IntAdid = "ca-app-pub-8745611599033887/2909019000";


    }


    public void showBanner(View view){
        String ASBannerAdid= "ca-app-pub-8808655139492822/3374543596";
        mAdMobBanner = new AdView(container.getContext());
        mAdMobBanner.setAdSize(AdSize.SMART_BANNER);
        mAdMobBanner.setAdUnitId(ASBannerAdid);
        mAdMobBanner.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() { System.out.println("MyApp onAdLoaded()"); }

            @Override
            public void onAdFailedToLoad(int errorCode) { System.out.println("MyApp onAdFailedToLoad()");}

            @Override
            public void onAdOpened() { System.out.println("MyApp onAdOpened()"); }

            @Override
            public void onAdLeftApplication() { System.out.println("MyApp onAdLeftApplication()"); }

            @Override
            public void onAdClosed() { System.out.println("MyApp onAdClosed()"); }
        });
        container.removeAllViews();
        container.addView(mAdMobBanner);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdMobBanner.loadAd(adRequest);
    }


    public void preloadInterstitial(View view){
        String interstitialAdid = "ca-app-pub-8808655139492822/4851276791";
        mAdMobInterstitial = new InterstitialAd(this);
        mAdMobInterstitial.setAdUnitId(interstitialAdid
        );

        mAdMobInterstitial.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() { System.out.println("MyApp onAdLoaded()"); }

            @Override
            public void onAdFailedToLoad(int errorCode) { System.out.println("MyApp onAdFailedToLoad()");}

            @Override
            public void onAdOpened() { System.out.println("MyApp onAdOpened()"); }

            @Override
            public void onAdLeftApplication() { System.out.println("MyApp onAdLeftApplication()"); }

            @Override
            public void onAdClosed() { System.out.println("MyApp onAdClosed()"); }
        });

        mAdMobInterstitial.loadAd(new AdRequest.Builder().build());
    }

    public void showInterstitial(View view){
        if (mAdMobInterstitial.isLoaded()) {
            mAdMobInterstitial.show();
        } else {
            Log.v(LOG_TAG, "AD NOT READY");
        }
    }
}
