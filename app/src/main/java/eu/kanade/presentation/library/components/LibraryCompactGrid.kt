package eu.kanade.presentation.library.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import eu.kanade.tachiyomi.ui.library.LibraryItem
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.manga.model.MangaCover

@Composable
internal fun LibraryCompactGrid(
    items: List<LibraryItem>,
    showTitle: Boolean,
    columns: Int,
    contentPadding: PaddingValues,
    selection: Set<Long>,
    onClick: (LibraryManga) -> Unit,
    onLongClick: (LibraryManga) -> Unit,
    onClickContinueReading: ((LibraryManga) -> Unit)?,
    searchQuery: String?,
    onGlobalSearchClicked: () -> Unit,
) {
    LazyLibraryGrid(
        modifier = Modifier.fillMaxSize(),
        columns = columns,
        contentPadding = contentPadding,
    ) {
        globalSearchItem(searchQuery, onGlobalSearchClicked)

        items(
            items = items,
            key = { it.libraryManga.id },
            contentType = { "library_compact_grid_item" },
        ) { libraryItem ->
            val manga = libraryItem.libraryManga.manga

            val totalChapters = libraryItem.libraryManga.totalChapters
            val progress = if (totalChapters > 0) {
                (libraryItem.libraryManga.readCount.toFloat() / totalChapters.toFloat()).coerceIn(0f, 1f)
            } else {
                null
            }

            val coverData = MangaCover(
                mangaId = manga.id,
                sourceId = manga.source,
                isMangaFavorite = manga.favorite,
                url = manga.thumbnailUrl,
                lastModified = manga.coverLastModified,
            )
            val continueReading: (() -> Unit)? =
                if (onClickContinueReading != null && libraryItem.unreadCount > 0) {
                    { onClickContinueReading(libraryItem.libraryManga) }
                } else {
                    null
                }

            if (showTitle) {
                LibraryShelfCard(
                    coverData = coverData,
                    title = manga.title,
                    progress = progress,
                    isSelected = manga.id in selection,
                    downloadCount = libraryItem.badges.downloadCount,
                    unreadCount = libraryItem.badges.unreadCount,
                    isLocal = libraryItem.badges.isLocal,
                    sourceLanguage = libraryItem.badges.sourceLanguage,
                    onLongClick = { onLongClick(libraryItem.libraryManga) },
                    onClick = { onClick(libraryItem.libraryManga) },
                    onClickContinueReading = continueReading,
                )
            } else {
                MangaCompactGridItem(
                    isSelected = manga.id in selection,
                    title = null,
                    coverData = coverData,
                    coverBadgeStart = {
                        DownloadsBadge(count = libraryItem.badges.downloadCount)
                        UnreadBadge(count = libraryItem.badges.unreadCount)
                    },
                    coverBadgeEnd = {
                        LanguageBadge(
                            isLocal = libraryItem.badges.isLocal,
                            sourceLanguage = libraryItem.badges.sourceLanguage,
                        )
                    },
                    progress = progress,
                    onLongClick = { onLongClick(libraryItem.libraryManga) },
                    onClick = { onClick(libraryItem.libraryManga) },
                    onClickContinueReading = continueReading,
                )
            }
        }
    }
}
