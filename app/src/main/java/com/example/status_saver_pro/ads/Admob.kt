package com.example.status_saver_pro.ads

import android.content.Context
import android.widget.LinearLayout
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize.BANNER
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener

class Admob {


    fun setBanner(banner: LinearLayout, context: Context) {
        if (AdUnit().isAd) {
            MobileAds.initialize(context, OnInitializationCompleteListener {

            })
            val adView = AdView(context)
            adView.adUnitId = AdUnit().bannerAd

            adView.setAdSize(BANNER)

            val adRequest = AdRequest.Builder().build()


            adView.loadAd(adRequest)

        }

    }
}