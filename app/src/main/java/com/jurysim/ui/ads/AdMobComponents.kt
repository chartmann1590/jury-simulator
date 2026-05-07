package com.jurysim.ui.ads

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.ViewGroup
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.jurysim.BuildConfig
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlin.random.Random

@Composable
fun GlobalBannerAd(modifier: Modifier = Modifier) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .navigationBarsPadding()
    ) {
        val adWidthPx = with(density) { maxWidth.roundToPx() }
        if (adWidthPx <= 0) return@BoxWithConstraints
        val adWidthDp = with(density) { adWidthPx.toDp().value.toInt().coerceAtLeast(1) }
        val adSize = remember(adWidthDp) {
            AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidthDp)
        }
        val adView = remember(adWidthDp) {
            AdView(context).apply {
                adUnitId = BuildConfig.ADMOB_BANNER_ID
                setAdSize(adSize)
                loadAd(AdRequest.Builder().build())
            }
        }
        DisposableEffect(adView) {
            onDispose { adView.destroy() }
        }

        AndroidView(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            factory = {
                adView.apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }
            }
        )
    }
}

class InterstitialAdController(private val context: Context) {
    private var interstitialAd: InterstitialAd? = null
    private var lastShownAtMs: Long = 0L

    fun preload() {
        if (interstitialAd != null) return
        InterstitialAd.load(
            context,
            BuildConfig.ADMOB_INTERSTITIAL_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    interstitialAd = null
                }
            }
        )
    }

    fun showForNewCaseIfReady() {
        showInternal(forceShow = true)
    }

    fun maybeShowDuringCase() {
        val cooldownElapsed = System.currentTimeMillis() - lastShownAtMs > 60_000
        if (!cooldownElapsed) return
        val shouldShow = Random.nextFloat() < 0.30f
        if (shouldShow) {
            showInternal(forceShow = false)
        }
    }

    private fun showInternal(forceShow: Boolean) {
        val ad = interstitialAd ?: run {
            preload()
            return
        }

        val activity = context.findActivity() ?: run {
            if (forceShow) preload()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                lastShownAtMs = System.currentTimeMillis()
                preload()
            }

            override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                interstitialAd = null
                preload()
            }
        }
        ad.show(activity)
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
