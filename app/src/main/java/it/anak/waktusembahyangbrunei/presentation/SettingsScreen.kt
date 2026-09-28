package it.anak.waktusembahyangbrunei.presentation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import it.anak.waktusembahyangbrunei.data.PrayerRepository
import it.anak.waktusembahyangbrunei.domain.AppStrings

@Composable
fun SettingsScreen(
    state: PrayerViewModel.UiState,
    onSelectDistrict: (String) -> Unit,
    onSelectLanguage: (String) -> Unit,
) {
    val listState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()
    val lang = state.lang

    ScreenScaffold(scrollState = listState) { contentPadding ->
        TransformingLazyColumn(contentPadding = contentPadding, state = listState) {
            item {
                ListHeader(
                    modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                    transformation = SurfaceTransformation(transformationSpec),
                ) {
                    Text(AppStrings.district(lang))
                }
            }
            items(PrayerRepository.DISTRICTS) { district ->
                OptionButton(
                    label = AppStrings.districtName(district),
                    selected = district == state.district,
                    onClick = { onSelectDistrict(district) },
                    modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                    transformation = SurfaceTransformation(transformationSpec),
                )
            }
            item {
                ListHeader(
                    modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                    transformation = SurfaceTransformation(transformationSpec),
                ) {
                    Text(AppStrings.language(lang))
                }
            }
            item {
                OptionButton(
                    label = "Bahasa Melayu",
                    selected = state.lang == PrayerRepository.LANG_MALAY,
                    onClick = { onSelectLanguage(PrayerRepository.LANG_MALAY) },
                    modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                    transformation = SurfaceTransformation(transformationSpec),
                )
            }
            item {
                OptionButton(
                    label = "English",
                    selected = state.lang == PrayerRepository.LANG_ENGLISH,
                    onClick = { onSelectLanguage(PrayerRepository.LANG_ENGLISH) },
                    modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                    transformation = SurfaceTransformation(transformationSpec),
                )
            }
        }
    }
}

@Composable
private fun OptionButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    transformation: SurfaceTransformation? = null,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        transformation = transformation,
        colors =
            if (selected) {
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                ButtonDefaults.buttonColors()
            },
    ) {
        Text(if (selected) "✓ $label" else label)
    }
}
