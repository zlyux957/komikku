// KMK -->
package eu.kanade.presentation.browse.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.icerock.moko.resources.StringResource
import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.source.model.Filter
import exh.source.getOriginalSource
import tachiyomi.i18n.kmk.KMR
import tachiyomi.presentation.core.components.HeadingItem
import tachiyomi.presentation.core.components.RadioItem
import tachiyomi.presentation.core.components.SettingsItemsPaddings
import tachiyomi.presentation.core.i18n.stringResource

internal object KavitaSortAdapter {
    // Match the source class accepted by the Kavita tracker, including all server instances.
    private const val SOURCE_CLASS = "eu.kanade.tachiyomi.extension.all.kavita.Kavita"
    private const val EXTENSION_PACKAGE = "eu.kanade.tachiyomi.extension.all.kavita"

    // Presentation labels only. The extension's field order and original strings stay intact.
    private val fields = linkedMapOf(
        "Sort name" to KMR.strings.kavita_sort_name,
        "Created" to KMR.strings.kavita_sort_created,
        "Last modified" to KMR.strings.kavita_sort_modified,
        "Item added" to KMR.strings.kavita_sort_added,
        "Time to Read" to KMR.strings.kavita_sort_reading_time,
        "Release year" to KMR.strings.kavita_sort_release_year,
        "Read Progress" to KMR.strings.kavita_sort_progress,
        "Average Rating" to KMR.strings.kavita_sort_rating,
        "Random" to KMR.strings.kavita_sort_random,
    )

    fun isKavitaSource(source: Source, extensionPackageForSource: (Long) -> String?): Boolean {
        val original = source.getOriginalSource()
        return original::class.qualifiedName == SOURCE_CLASS &&
            extensionPackageForSource(original.id) == EXTENSION_PACKAGE
    }

    fun labels(filter: Filter.Sort): List<StringResource>? {
        if (filter.name != "Sort by" || filter.values.asList() != fields.keys.toList()) return null
        if (filter.state?.let { it.index !in filter.values.indices } == true) return null
        return fields.values.toList()
    }

    fun selectField(filter: Filter.Sort, index: Int) {
        filter.state = Filter.Sort.Selection(index, filter.state?.ascending ?: true)
    }

    fun selectDirection(filter: Filter.Sort, ascending: Boolean) {
        // The recognized schema starts with Sort name, matching Kavita's default selection.
        filter.state = Filter.Sort.Selection(filter.state?.index ?: 0, ascending)
    }
}

@Composable
internal fun KavitaSortItem(
    filter: Filter.Sort,
    labels: List<StringResource>,
    onUpdate: () -> Unit,
) {
    Column {
        HeadingItem(stringResource(KMR.strings.kavita_sort_field))
        labels.forEachIndexed { index, label ->
            RadioItem(
                label = stringResource(label),
                selected = filter.state?.index == index,
                onClick = {
                    KavitaSortAdapter.selectField(filter, index)
                    onUpdate()
                },
            )
        }
        HeadingItem(stringResource(KMR.strings.kavita_sort_direction))
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = SettingsItemsPaddings.Horizontal,
                    vertical = SettingsItemsPaddings.Vertical,
                ),
        ) {
            listOf(true, false).forEachIndexed { index, ascending ->
                SegmentedButton(
                    selected = filter.state?.ascending == ascending,
                    onClick = {
                        KavitaSortAdapter.selectDirection(filter, ascending)
                        onUpdate()
                    },
                    shape = SegmentedButtonDefaults.itemShape(index, 2),
                ) {
                    Text(
                        stringResource(
                            if (ascending) KMR.strings.kavita_sort_ascending else KMR.strings.kavita_sort_descending,
                        ),
                    )
                }
            }
        }
    }
}
// KMK <--
