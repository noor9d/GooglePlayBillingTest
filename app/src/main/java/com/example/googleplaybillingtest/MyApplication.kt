package com.example.googleplaybillingtest

import android.app.Application
import android.util.Log

class MyApplication : Application() {

    companion object {
        const val TAG = "MyApplication1"
        var removeAdsStatus = false
        const val REMOVE_ADS_KEY = "remove_ads_key"
    }

    override fun onCreate() {
        super.onCreate()

        removeAdsStatus = getRemoveAdsStatus()

        if (!removeAdsStatus) {
            Log.d(TAG, "onCreate: status= $removeAdsStatus")
            // initialize google ads
//            MobileAds.initialize(this) {}
//            AudienceNetworkAds.initialize(this)
        }
    }

    private fun getRemoveAdsStatus(): Boolean {
        val sharedPref = applicationContext.getSharedPreferences("remove_ads_prefs", MODE_PRIVATE)
        return sharedPref.getBoolean(REMOVE_ADS_KEY, false)
    }
}