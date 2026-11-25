package mihon.core.migration.migrations

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import mihon.core.migration.Migration
import mihon.core.migration.MigrationContext
import tachiyomi.domain.library.service.LibraryPreferences
import tachiyomi.domain.library.service.LibraryPreferences.Companion.MANGA_HAS_UNREAD
import tachiyomi.domain.library.service.LibraryPreferences.Companion.MANGA_NON_COMPLETED
import tachiyomi.domain.library.service.LibraryPreferences.Companion.MANGA_NON_READ
import tachiyomi.domain.library.service.LibraryPreferences.Companion.MANGA_OUTSIDE_RELEASE_PERIOD

@Inject
@ContributesIntoSet(AppScope::class)
class LibraryUpdateRestrictionsMigration(
    private val libraryPreferences: LibraryPreferences,
) : Migration {
    override val version: Float = 4f

    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        // The old factory defaults kept most libraries from ever being checked. Reset only untouched
        // sets so a deliberate choice is preserved.
        val legacyDefaults = setOf(
            MANGA_HAS_UNREAD,
            MANGA_NON_COMPLETED,
            MANGA_NON_READ,
            MANGA_OUTSIDE_RELEASE_PERIOD,
        )
        if (libraryPreferences.autoUpdateMangaRestrictions.get() == legacyDefaults) {
            libraryPreferences.autoUpdateMangaRestrictions.delete()
        }
        return true
    }
}
