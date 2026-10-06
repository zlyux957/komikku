// KMK -->
package eu.kanade.presentation.browse.components

import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.source.model.Filter
import eu.kanade.tachiyomi.source.model.FilterList
import io.mockk.mockk
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import xyz.nulldev.ts.api.http.serializer.FilterSerializer

class KavitaSortAdapterTest {
    private fun sort(state: Filter.Sort.Selection? = null) = object : Filter.Sort(
        "Sort by",
        arrayOf(
            "Sort name", "Created", "Last modified", "Item added", "Time to Read",
            "Release year", "Read Progress", "Average Rating", "Random",
        ),
        state,
    ) {}

    @Test
    fun `field changes preserve direction and extension data`() {
        val filter = sort(Filter.Sort.Selection(2, false))
        val values = filter.values
        val originalValues = values.copyOf()
        val filters = FilterList(filter)

        KavitaSortAdapter.selectField(filter, 6)
        KavitaSortAdapter.selectField(filter, 6)

        assertEquals(Filter.Sort.Selection(6, false), filter.state)
        assertSame(filter, filters[0])
        assertSame(values, filter.values)
        assertEquals(originalValues.toList(), filter.values.toList())
        assertEquals("Sort by", filter.name)
    }

    @Test
    fun `direction changes preserve field`() {
        val filter = sort(Filter.Sort.Selection(2, true))
        KavitaSortAdapter.selectDirection(filter, false)
        assertEquals(Filter.Sort.Selection(2, false), filter.state)
        KavitaSortAdapter.selectDirection(filter, true)
        assertEquals(Filter.Sort.Selection(2, true), filter.state)
    }

    @Test
    fun `null state uses ascending for a field and name for a direction`() {
        val fieldFilter = sort()
        KavitaSortAdapter.selectField(fieldFilter, 6)
        assertEquals(Filter.Sort.Selection(6, true), fieldFilter.state)

        val directionFilter = sort()
        KavitaSortAdapter.selectDirection(directionFilter, false)
        assertEquals(Filter.Sort.Selection(0, false), directionFilter.state)
    }

    @Test
    fun `only the known ordered schema with valid state is supported`() {
        assertNotNull(KavitaSortAdapter.labels(sort()))
        assertNotNull(KavitaSortAdapter.labels(sort(Filter.Sort.Selection(8, false))))
        assertNull(KavitaSortAdapter.labels(sort(Filter.Sort.Selection(-1, true))))
        assertNull(KavitaSortAdapter.labels(sort(Filter.Sort.Selection(9, true))))

        val reordered = sort().apply {
            values[0] = "Created"
            values[1] = "Sort name"
        }
        assertNull(KavitaSortAdapter.labels(reordered))
        assertNull(KavitaSortAdapter.labels(sort().apply { values[0] = "Unknown" }))
        assertNull(KavitaSortAdapter.labels(object : Filter.Sort("Other sort", sort().values) {}))
        assertNull(KavitaSortAdapter.labels(object : Filter.Sort("Sort by", emptyArray()) {}))
    }

    @Test
    fun `package alone never identifies an unrelated source as Kavita`() {
        var packageLookedUp = false
        assertFalse(
            KavitaSortAdapter.isKavitaSource(mockk<Source>()) {
                packageLookedUp = true
                "eu.kanade.tachiyomi.extension.all.kavita"
            },
        )
        assertFalse(packageLookedUp)
    }

    @Test
    fun `adapted selection round trips through existing saved search serialization`() {
        val filter = sort(Filter.Sort.Selection(2, true))
        KavitaSortAdapter.selectField(filter, 6)
        KavitaSortAdapter.selectDirection(filter, false)

        val serializer = FilterSerializer()
        val json = Json.parseToJsonElement(serializer.serialize(FilterList(filter)).toString())
        val restored = sort()
        serializer.deserialize(FilterList(restored), json as kotlinx.serialization.json.JsonArray)

        assertEquals(filter.state, restored.state)
        assertEquals(filter.values.toList(), restored.values.toList())
        assertEquals(filter.name, restored.name)
    }
}
// KMK <--
