package it.anak.waktusembahyangbrunei.tile

import android.content.Context
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ResourceBuilders.Resources
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.material3.Typography.BODY_SMALL
import androidx.wear.protolayout.material3.Typography.LABEL_SMALL
import androidx.wear.protolayout.material3.Typography.TITLE_MEDIUM
import androidx.wear.protolayout.material3.materialScope
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.types.layoutString
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.RequestBuilders.ResourcesRequest
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TileService
import androidx.wear.tiles.tooling.preview.Preview
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import it.anak.waktusembahyangbrunei.WaktuApp
import it.anak.waktusembahyangbrunei.data.bruneiNow
import it.anak.waktusembahyangbrunei.domain.AppStrings
import it.anak.waktusembahyangbrunei.domain.NextPrayer
import it.anak.waktusembahyangbrunei.domain.NextPrayerCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.guava.future

private const val RESOURCES_VERSION = "0"
private const val FRESHNESS_MS = 60_000L

private data class TileData(
    val lang: String,
    val header: String,
    val next: NextPrayer?,
)

class MainTileService : TileService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onTileRequest(
        requestParams: RequestBuilders.TileRequest
    ): ListenableFuture<TileBuilders.Tile> =
        serviceScope.future { tile(requestParams, this@MainTileService, loadTileData()) }

    override fun onTileResourcesRequest(
        requestParams: ResourcesRequest
    ): ListenableFuture<Resources> = Futures.immediateFuture(resources(requestParams))

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    private suspend fun loadTileData(): TileData {
        val repo = (applicationContext as WaktuApp).repository
        val lang = repo.currentLanguage()
        val result = repo.getDay()
        val schedule = result.response?.data?.schedule.orEmpty()
        val now = bruneiNow().toLocalDateTime()
        var next = NextPrayerCalculator.next(schedule, now)
        if (next?.isTomorrow == true) {
            val imsak =
                NextPrayerCalculator.parseTime(
                    repo.getTomorrow().response?.data?.timings?.imsak
                )
            next = NextPrayerCalculator.next(schedule, now, imsak)
        }
        return TileData(lang = lang, header = result.response?.data?.hijri.orEmpty(), next = next)
    }
}

private fun resources(requestParams: ResourcesRequest): Resources =
    Resources.Builder().setVersion(RESOURCES_VERSION).build()

private fun launchClickable(context: Context): ModifiersBuilders.Clickable =
    ModifiersBuilders.Clickable.Builder()
        .setId("open_app")
        .setOnClick(
            ActionBuilders.LaunchAction.Builder()
                .setAndroidActivity(
                    ActionBuilders.AndroidActivity.Builder()
                        .setPackageName(context.packageName)
                        .setClassName(
                            "it.anak.waktusembahyangbrunei.presentation.MainActivity"
                        )
                        .build()
                )
                .build()
        )
        .build()

private fun tile(
    requestParams: RequestBuilders.TileRequest,
    context: Context,
    data: TileData,
): TileBuilders.Tile {
    val mainText =
        data.next?.let { "${AppStrings.prayerName(it.key, data.lang)} ${it.time}" }
            ?: AppStrings.unavailable(data.lang)
    val bottomText =
        data.next?.let {
            AppStrings.inDuration(NextPrayerCalculator.formatDuration(it.until), data.lang)
        } ?: AppStrings.tapToRetry(data.lang)

    return TileBuilders.Tile.Builder()
        .setResourcesVersion(RESOURCES_VERSION)
        .setFreshnessIntervalMillis(FRESHNESS_MS)
        .setTileTimeline(
            TimelineBuilders.Timeline.fromLayoutElement(
                materialScope(context, requestParams.deviceConfiguration) {
                    primaryLayout(
                        onClick = launchClickable(context),
                        titleSlot = {
                            text(data.header.layoutString, typography = LABEL_SMALL)
                        },
                        mainSlot = {
                            text(mainText.layoutString, typography = TITLE_MEDIUM)
                        },
                        bottomSlot = {
                            text(bottomText.layoutString, typography = BODY_SMALL)
                        },
                    )
                }
            )
        )
        .build()
}

@Preview(device = WearDevices.SMALL_ROUND)
@Preview(device = WearDevices.LARGE_ROUND)
fun tilePreview(context: Context) = TilePreviewData(::resources) {
    tile(
        it,
        context,
        TileData(
            lang = "ms",
            header = "16 Rabiulakhir 1448",
            next = null,
        ),
    )
}
