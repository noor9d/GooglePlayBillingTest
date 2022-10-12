package com.example.googleplaybillingtest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.android.billingclient.api.*
import com.example.googleplaybillingtest.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var billingClient: BillingClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
            // To be implemented in a later section.
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK &&
                purchases.isNullOrEmpty()
            ) {
                for (purchase in purchases!!) {
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                        !purchase.isAcknowledged
                    ) {

                    }
                }
            }
        }

        billingClient = BillingClient.newBuilder(this)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()

        connectToGooglePlayBilling()

        binding.icClose.setOnClickListener {
            finish()
        }
    }

    private fun connectToGooglePlayBilling() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    Log.d(TAG, "onBillingSetupFinished: ")
                    getProductDetails()
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                Log.d(TAG, "onBillingServiceDisconnected: ")
                connectToGooglePlayBilling()
            }
        })
    }

    private fun getProductDetails() {
        val productList = ArrayList<String>()
        productList.add("android.test.purchased")

        val params = SkuDetailsParams
            .newBuilder()
            .setSkusList(productList)
            .setType(BillingClient.SkuType.INAPP)
            .build()

        billingClient.querySkuDetailsAsync(params) { billingResult, skuDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK &&
                skuDetailsList.isNullOrEmpty()
            ) {
                val itemInfo = skuDetailsList?.get(0)
                Log.d(TAG, "getProductDetails: item name= ${itemInfo?.title}")
                Log.d(TAG, "getProductDetails: item price= ${itemInfo?.price}")

                binding.button.setOnClickListener {
                    val flowPurchase = BillingFlowParams.newBuilder()
                        .setSkuDetails(itemInfo!!)
                        .build()

                    billingClient.launchBillingFlow(
                        this@MainActivity,
                        flowPurchase
                    )
                }
            }
        }
    }

    /*private fun getProductDetails2() {
        val list = mutableListOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("product_id_example")
                .setProductType(BillingClient.ProductType.INAPP)
                .build())

        val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
                .setProductList(list)
                .build()

        billingClient.queryProductDetailsAsync(queryProductDetailsParams) {
                billingResult, productDetailsList ->
            // check billingResult
            // process returned productDetailsList
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK &&
                productDetailsList.isNotEmpty()
            ) {
                val itemInfo = productDetailsList[0]
                Log.d(TAG, "getProductDetails: item name= ${itemInfo.title}")
                Log.d(TAG, "getProductDetails: item price= ${itemInfo.oneTimePurchaseOfferDetails}")

                binding.button.setOnClickListener {
                    val flowPurchase = BillingFlowParams.newBuilder()
                        .setSkuDetails(itemInfo!!)
                        .build()

                    billingClient.launchBillingFlow(
                        this@MainActivity,
                        flowPurchase
                    )
                }
            }
        }
    }*/

    companion object {
        const val TAG = "MainActivity1"
    }
}