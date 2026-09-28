package it.anak.waktusembahyangbrunei.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import it.anak.waktusembahyangbrunei.data.ScheduleEntry
import it.anak.waktusembahyangbrunei.domain.AppStrings
import it.anak.waktusembahyangbrunei.domain.NextPrayerCalculator

@Composable
fun PrayerTimesScreen(
    state: PrayerViewModel.UiState,
    onRefresh: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val listState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()
    val lang = state.lang

    ScreenScaffold(
        scrollState = listState,
        edgeButton = {
            EdgeButton(onClick = onOpenSettings) { Text(AppStrings.settings(lang)) }
        },
    ) { contentPadding ->
        TransformingLazyColumn(contentPadding = contentPadding, state = listState) {
            item {
                ListHeader(
                    modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                    transformation = SurfaceTransformation(transformationSpec),
                ) {
                    Text("${state.day} · ${state.dateMalay}")
                }
            }
            if (state.hijri.isNotEmpty()) {
                item {
                    Text(
                        text = "${state.hijri} · ${AppStrings.districtName(state.district)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
            }
            state.next?.let { next ->
                item {
                    val name = AppStrings.prayerName(next.key, lang)
                    val countdown =
                        AppStrings.nextIn(
                            name,
                            NextPrayerCalculator.formatDuration(next.until),
                            lang,
                        )
                    Text(
                        text =
                            if (next.isTomorrow) {
                                "$countdown (${AppStrings.tomorrow(lang)})"
                            } else {
                                countdown
                            },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            if (state.stale) {
                item {
                    Text(
                        text = AppStrings.offline(lang),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }
            if (state.loading) {
                item { Text(AppStrings.loading(lang)) }
            }
            state.error?.let { error ->
                item { Text(text = error, color = MaterialTheme.colorScheme.error) }
            }
            items(state.entries) { entry ->
                PrayerRow(
                    entry = entry,
                    lang = lang,
                    isNext = entry.key == state.next?.key && state.next?.isTomorrow == false,
                    modifier =
                        Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                )
            }
            item {
                Button(
                    onClick = onRefresh,
                    modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                    transformation = SurfaceTransformation(transformationSpec),
                ) {
                    Text(AppStrings.refresh(lang))
                }
            }
        }
    }
}

@Composable
private fun PrayerRow(
    entry: ScheduleEntry,
    lang: String,
    isNext: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = AppStrings.prayerName(entry.key, lang),
            color =
                if (isNext) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface,
            style = if (isNext) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = entry.time,
            color =
                if (isNext) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface,
            style = if (isNext) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
        )
    }
}
