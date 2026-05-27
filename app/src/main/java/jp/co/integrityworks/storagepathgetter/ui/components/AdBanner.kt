package jp.co.integrityworks.storagepathgetter.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import jp.co.integrityworks.storagepathgetter.R
import jp.co.integrityworks.storagepathgetter.ui.theme.Dimens

@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    if (LocalInspectionMode.current) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(Dimens.AdBannerPlaceholderHeight)
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Ad Banner Placeholder")
        }
        return
    }

    val adUnitId = stringResource(id = R.string.ad_unit_id)
    AndroidView(
        modifier = modifier,
        factory = { viewContext ->
            AdView(viewContext).apply {
                val displayMetrics = viewContext.resources.displayMetrics
                val widthInDp = (displayMetrics.widthPixels / displayMetrics.density).toInt()

                @Suppress("DEPRECATION")
                val adSize =
                    AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(viewContext, widthInDp)

                setAdSize(adSize)
                this.adUnitId = adUnitId
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
