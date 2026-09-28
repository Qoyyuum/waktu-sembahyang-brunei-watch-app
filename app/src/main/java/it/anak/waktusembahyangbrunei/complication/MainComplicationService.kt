package it.anak.waktusembahyangbrunei.complication

import android.app.PendingIntent
import android.content.Intent
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import it.anak.waktusembahyangbrunei.WaktuApp
import it.anak.waktusembahyangbrunei.data.bruneiNow
import it.anak.waktusembahyangbrunei.domain.AppStrings
import it.anak.waktusembahyangbrunei.domain.NextPrayer
import it.anak.waktusembahyangbrunei.domain.NextPrayerCalculator
import it.anak.waktusembahyangbrunei.presentation.MainActivity

/** Provides the next prayer name/time (and countdown) to any watch face. */
class MainComplicationService : SuspendingComplicationDataSourceService() {

    override fun getPreviewData(type: ComplicationType): ComplicationData? =
        when (type) {
            ComplicationType.SHORT_TEXT ->
                createShortText("Maghrib 18:14", "Maghrib")
            ComplicationType.LONG_TEXT ->
                createLongText("Maghrib", "dalam 2j 14m")
            else -> null
        }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        val repo = (applicationContext as WaktuApp).repository
        val lang = repo.currentLanguage()
        val schedule = repo.getDay().response?.data?.schedule.orEmpty()
        val now = bruneiNow().toLocalDateTime()
        val next: NextPrayer = NextPrayerCalculator.next(schedule, now) ?: return null
        val name = AppStrings.prayerName(next.key, lang)

        return when (request.complicationType) {
            ComplicationType.SHORT_TEXT -> createShortText("$name ${next.time}", name)
            ComplicationType.LONG_TEXT ->
                createLongText(
                    name,
                    AppStrings.inDuration(
                        NextPrayerCalculator.formatDuration(next.until),
                        lang,
                    ),
                )
            else -> null
        }
    }

    private fun tapAction(): PendingIntent =
        PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

    private fun createShortText(text: String, contentDescription: String) =
        ShortTextComplicationData.Builder(
                text = PlainComplicationText.Builder(text).build(),
                contentDescription = PlainComplicationText.Builder(contentDescription).build(),
            )
            .setTapAction(tapAction())
            .build()

    private fun createLongText(title: String, text: String) =
        LongTextComplicationData.Builder(
                text = PlainComplicationText.Builder("$title $text").build(),
                contentDescription = PlainComplicationText.Builder("$title $text").build(),
            )
            .setTitle(PlainComplicationText.Builder(title).build())
            .setTapAction(tapAction())
            .build()
}
