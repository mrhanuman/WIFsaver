package com.example.status_saver_pro.ads

import android.content.Context
import android.widget.LinearLayout
import com.google.android.gms.ads.*
import com.google.android.gms.ads.AdSize.BANNER

class Admob {



    fun setBanner(banner: LinearLayout, context: Context) {
        if (AdUnit().isAd) {
            MobileAds.initialize(context){
                RequestConfiguration.Builder().build()
            }
            val adView = AdView(context)
            banner.addView(adView)
            adView.adUnitId = AdUnit().bannerAd


            adView.setAdSize(BANNER)

            val adRequest = AdRequest.Builder().build()


            adView.loadAd(adRequest)
        }


    }
}