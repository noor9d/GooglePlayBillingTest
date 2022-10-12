package com.example.googleplaybillingtest

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.googleplaybillingtest.databinding.ActivityPremiumBinding
import games.moisoni.google_iab.BillingConnector
import games.moisoni.google_iab.BillingEventListener
import games.moisoni.google_iab.enums.ErrorType
import games.moisoni.google_iab.enums.ProductType
import games.moisoni.google_iab.models.BillingResponse
import games.moisoni.google_iab.models.ProductInfo
import games.moisoni.google_iab.models.PurchaseInfo

class PremiumActivity : AppCompatActivity() {
    private val binding: ActivityPremiumBinding by lazy {
        ActivityPremiumBinding.inflate(layoutInflater)
    }

    private var billingConnector: BillingConnector? = null
    private var userPrefersAdFree = false

    companion object {
        const val TAG = "PremiumActivity1"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initializeBillingClient()
        setupClickListeners()
    }

    private fun initializeBillingClient() {
        val nonConsumableIds: MutableList<String> = ArrayList()
        nonConsumableIds.add(getString(R.string.remove_ads_play_console_id))

        billingConnector = BillingConnector(
            this,
            getString(R.string.license_key_play_console)
        ).setNonConsumableIds(nonConsumableIds)
            .autoAcknowledge()
            .autoConsume()
            .enableLogging()
            .connect()

        billingConnector?.setBillingEventListener(object :
            BillingEventListener {
            override fun onProductsFetched(productDetails: List<ProductInfo?>) {
                // Provides a list with fetched products
                val product = productDetails[0]
                Log.d(TAG, "onProductsFetched: name= ${product?.name}")
                Log.d(TAG, "onProductsFetched: desc= ${product?.description}")
                Log.d(TAG, "onProductsFetched: price= ${product?.oneTimePurchaseOfferDetails?.zza().toString()}")

                binding.title.text = product?.name
                binding.description.text = product?.description
                if (product?.oneTimePurchaseOfferDetails?.zza().toString().isNotEmpty())
                    binding.button.text = product?.oneTimePurchaseOfferDetails?.zza().toString()
                else
                    binding.button.text = resources.getString(R.string.purchase)
            }

            //this is the listener in which we can restore previous purchases
            override fun onPurchasedProductsFetched(
                productType: ProductType,
                purchases: List<PurchaseInfo?>
            ) {
                Log.d(TAG, "onPurchasedProductsFetched: called!")

                var purchasedProduct: String
                var isAcknowledged: Boolean
                for (purchaseInfo in purchases) {
                    purchasedProduct = purchaseInfo?.product.toString()
                    isAcknowledged = purchaseInfo?.isAcknowledged == true

//                    if (!userPrefersAdFree) {
                        if (purchasedProduct.equals(
                                getString(R.string.remove_ads_play_console_id),
                                ignoreCase = true
                            )
                        ) {
                            if (isAcknowledged) {
                                // here we are saving the purchase status into sharedPreferences
                                saveRemoveAdsStatus(true)
                                Log.d(
                                    TAG,
                                    "onPurchasedProductsFetched: Purchase Successfully Restored!"
                                )

                                Toast.makeText(
                                    this@PremiumActivity,
                                    "The previous purchase was successfully restored.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
//                    }
                }
            }

            override fun onProductsPurchased(purchases: List<PurchaseInfo?>) {
                // Callback after a product is purchased
                Log.d(TAG, "onProductsPurchased: called!")
            }

            override fun onPurchaseAcknowledged(purchase: PurchaseInfo) {
                // Callback after a purchase is acknowledged

                /*
                 * Grant user entitlement for NON-CONSUMABLE products and SUBSCRIPTIONS here
                 *
                 * Even though onProductsPurchased is triggered when a purchase is successfully made
                 * there might be a problem along the way with the payment and the purchase won't be acknowledged
                 *
                 * Google will refund users purchases that aren't acknowledged in 3 days
                 *
                 * To ensure that all valid purchases are acknowledged the library will automatically
                 * check and acknowledge all unacknowledged products at the startup
                 * */

                val acknowledgedProduct = purchase.product
                if (acknowledgedProduct.equals(
                        getString(R.string.remove_ads_play_console_id),
                        ignoreCase = true
                    )
                ) {

                    // here we are saving the purchase status into sharedPreferences
                    saveRemoveAdsStatus(true)
                    Log.d(TAG, "onPurchaseAcknowledged: Purchase Successful!")

                    Toast.makeText(
                        this@PremiumActivity,
                        "The purchase was successfully made!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onPurchaseConsumed(purchase: PurchaseInfo) {
                // Callback after a purchase is consumed

                /*
                 * Grant user entitlement for CONSUMABLE products here
                 *
                 * Even though onProductsPurchased is triggered when a purchase is successfully made
                 * there might be a problem along the way with the payment and the user will be able consume the product
                 * without actually paying
                 * */

                Log.d(TAG, "onPurchaseConsumed: called!")
            }

            override fun onBillingError(
                billingConnector: BillingConnector,
                response: BillingResponse
            ) {
                // Callback after an error occurs
                when (response.errorType) {
                    ErrorType.ACKNOWLEDGE_WARNING ->
                        /*
                         * This will be triggered when a purchase can not be acknowledged because the state is PENDING
                         * A purchase can be acknowledged only when the state is PURCHASED
                         *
                         * PENDING transactions usually occur when users choose cash as their form of payment
                         *
                         * Here users can be informed that it may take a while until the purchase complete
                         * and to come back later to receive their purchase
                         * */
                        Toast.makeText(
                            this@PremiumActivity,
                            "The transaction is still pending. Please come back later to receive the purchase!",
                            Toast.LENGTH_SHORT
                        ).show()

                    ErrorType.BILLING_UNAVAILABLE, ErrorType.SERVICE_UNAVAILABLE ->
                        Toast.makeText(
                            this@PremiumActivity,
                            "Billing is unavailable at the moment. Check your internet connection!",
                            Toast.LENGTH_SHORT
                        ).show()

                    ErrorType.ERROR -> Toast.makeText(
                        this@PremiumActivity,
                        "Something happened, the transaction was canceled!",
                        Toast.LENGTH_SHORT
                    ).show()

                    else -> {
                        Toast.makeText(
                            this@PremiumActivity,
                            "${response.errorType}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }

    private fun setupClickListeners() {
        binding.icClose.setOnClickListener {
            finish()
        }

        binding.button.setOnClickListener {
            billingConnector?.purchase(
                this@PremiumActivity,
                getString(R.string.remove_ads_play_console_id)
            )
        }
    }

    private fun saveRemoveAdsStatus(removeAdStatus: Boolean) {
        val sharedPref =
            applicationContext.getSharedPreferences("remove_ads_prefs", MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putBoolean(MyApplication.REMOVE_ADS_KEY, removeAdStatus)
            apply()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (billingConnector != null) {
            billingConnector?.release()
        }
    }
}