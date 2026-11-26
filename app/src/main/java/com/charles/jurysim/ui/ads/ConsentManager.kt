package com.charles.jurysim.ui.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Google UMP consent gate for AdMob.
 *
 * Google Play requires a consent flow before ads are requested in
 * regions covered by GDPR/UK rules, and MobileAds must not be
 * initialized before the user has had the chance to consent. This
 * manager is the single entry point:
 *
 * 1. [requestConsentAndInitAds] is called from `MainActivity.onCreate`.
 * 2. It refreshes consent info, shows the consent form if required,
 *    and only then initializes the Mobile Ads SDK.
 * 3. [canRequestAds] flips to true once consent allows ad requests
 *    (immediately, if consent was already gathered in a prior session).
 *
 * The banner and interstitial components observe [canRequestAds] and
 * stay completely idle until it is true.
 */
object ConsentManager {

    private val _canRequestAds = MutableStateFlow(false)
    val canRequestAds: StateFlow<Boolean> = _canRequestAds.asStateFlow()

    private val sdkInitialized = AtomicBoolean(false)

    fun requestConsentAndInitAds(activity: Activity) {
        val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
        val params = ConsentRequestParameters.Builder().build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                // Consent info updated; show the form if one is required.
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { _ ->
                    if (consentInformation.canRequestAds()) {
                        initializeAds(activity.applicationContext)
                    }
                }
            },
            { _ ->
                // Consent info update failed (e.g. offline). Honor any
                // previously gathered consent state.
                if (consentInformation.canRequestAds()) {
                    initializeAds(activity.applicationContext)
                }
            }
        )

        // Consent already gathered in a previous session: no form is
        // needed and ads may be requested right away.
        if (consentInformation.canRequestAds()) {
            initializeAds(activity.applicationContext)
        }
    }

    private fun initializeAds(context: Context) {
        if (!sdkInitialized.compareAndSet(false, true)) {
            _canRequestAds.value = true
            return
        }
        MobileAds.initialize(context) {
            _canRequestAds.value = true
        }
        // Ad views may load as soon as initialize() has been *called*;
        // the callback above is only an optimization signal.
        _canRequestAds.value = true
    }
}
