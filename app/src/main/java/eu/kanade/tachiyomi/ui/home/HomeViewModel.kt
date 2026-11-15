package eu.kanade.tachiyomi.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingSource
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import eu.kanade.domain.source.service.SourcePreferences
import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.source.model.Filter
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import mihon.domain.extension.model.ContentWarning
import mihon.domain.manga.model.toDomainManga
import tachiyomi.domain.chapter.model.Chapter
import tachiyomi.domain.history.interactor.GetHistory
import tachiyomi.domain.history.interactor.GetNextChapters
import tachiyomi.domain.history.model.HistoryWithRelations
import tachiyomi.domain.manga.interactor.GetLibraryManga
import tachiyomi.domain.manga.interactor.NetworkToLocalManga
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.manga.model.MangaCover
import tachiyomi.domain.manga.model.asMangaCover
import tachiyomi.domain.source.repository.SourceRepository
import tachiyomi.domain.source.service.SourceManager
import tachiyomi.domain.updates.interactor.GetUpdates
import tachiyomi.domain.updates.model.UpdatesWithRelations
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class, binding = binding<ViewModel>())
class HomeViewModel(
    getUpdates: GetUpdates,
    getHistory: GetHistory,
    getLibraryManga: GetLibraryManga,
    private val getNextChapters: GetNextChapters,
    private val networkToLocalManga: NetworkToLocalManga,
    private val sourceRepository: SourceRepository,
    private val sourceManager: SourceManager,
    private val sourcePreferences: SourcePreferences,
) : ViewModel() {

    val updates: StateFlow<List<UpdatesWithRelations>> = getUpdates.subscribe(
        instant = Clock.System.now().minus(3, DateTimeUnit.MONTH, TimeZone.currentSystemDefault()),
        unread = null,
        started = null,
        bookmarked = null,
        hideExcludedScanlators = false,
        includedCategories = emptyList(),
        excludedCategories = emptyList(),
    )
        .map { list ->
            list.sortedByDescending { it.dateFetch }
                .distinctBy { it.mangaId }
                .take(20)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val history: StateFlow<List<HomeHistoryItem>> = combine(
        getHistory.subscribe("").map { it.take(10) },
        getLibraryManga.subscribe(),
    ) { historyList, libraryList ->
        historyList.map { hist ->
            val libManga = libraryList.find { it.manga.id == hist.mangaId }
            val progress = if (libManga != null && libManga.totalChapters > 0) {
                (libManga.readCount.toFloat() / libManga.totalChapters.toFloat()).coerceIn(0f, 1f)
            } else {
                null
            }

            HomeHistoryItem(
                history = hist,
                progress = progress,
                sourceId = libManga?.manga?.source ?: 0L,
                favorite = libManga?.manga?.favorite ?: false,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _genreSamples = MutableStateFlow<List<HomeGenreSample>>(emptyList())
    val genreSamples: StateFlow<List<HomeGenreSample>> = _genreSamples.asStateFlow()
    private var samplesLoadedAt: Instant? = null
    private var samplesLoading = false

    /**
     * Ejemplos por genero traidos de la propia fuente: tres portadas distintas por genero, sin
     * repetir una obra entre generos. Se piden despues del banner para no retrasar el Inicio.
     */
    fun refreshGenreSamples(force: Boolean = false) {
        val loadedAt = samplesLoadedAt
        if (!force && loadedAt != null && Clock.System.now() - loadedAt < HERO_CACHE_TTL) return
        if (samplesLoading) return
        samplesLoading = true
        viewModelScope.launch {
            val loaded = runCatching { loadGenreSamples() }.getOrDefault(emptyList())
            if (loaded.isNotEmpty()) {
                _genreSamples.value = loaded
                samplesLoadedAt = Clock.System.now()
            }
            samplesLoading = false
        }
    }

    private val _hero = MutableStateFlow(HomeHeroState())
    val hero: StateFlow<HomeHeroState> = _hero.asStateFlow()
    private var heroLoadedAt: Instant? = null
    private var heroLoading = false

    fun refreshHero(force: Boolean = false) {
        val loadedAt = heroLoadedAt
        if (!force && loadedAt != null && Clock.System.now() - loadedAt < HERO_CACHE_TTL) return
        if (heroLoading) return
        heroLoading = true
        viewModelScope.launch {
            val loaded = runCatching { loadHero() }.getOrNull()
            if (loaded != null) {
                _hero.value = loaded
                heroLoadedAt = Clock.System.now()
            }
            heroLoading = false
        }
    }

    private suspend fun loadHero(): HomeHeroState = withContext(Dispatchers.IO) {
        val source = mostUsedOnlineSource() ?: return@withContext HomeHeroState()
        val sourceGenres = runCatching { genreNames(source) }.getOrDefault(emptyList())
        val showNsfw = ContentWarning.NSFW in sourcePreferences.enabledContentWarnings.get()

        val page = sourceRepository.getPopular(source.id).load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 30, placeholdersEnabled = false),
        ) as? PagingSource.LoadResult.Page

        val items = page?.data.orEmpty()
            .asSequence()
            .filter { showNsfw || !hasAdultGenre(it) }
            .filter { it.title.isNotBlank() }
            .take(HERO_ITEM_COUNT)
            .map { manga ->
                HomeHeroItem(
                    mangaId = manga.id,
                    title = manga.title,
                    coverData = manga.asMangaCover(),
                    subtitle = heroSubtitle(manga, source.name),
                )
            }
            .toList()

        HomeHeroState(
            items = items,
            sourceId = source.id,
            sourceLabel = if (source.lang.isBlank()) source.name else "${source.name} (${source.lang.uppercase()})",
            sourceGenres = sourceGenres,
        )
    }

    private suspend fun loadGenreSamples(): List<HomeGenreSample> = withContext(Dispatchers.IO) {
        val source = mostUsedOnlineSource() ?: return@withContext emptyList()
        val genres = runCatching { genreNames(source) }.getOrDefault(emptyList())
        val showNsfw = ContentWarning.NSFW in sourcePreferences.enabledContentWarnings.get()
        val seen = mutableSetOf<String>()

        genres.take(GENRE_SAMPLE_LIMIT).mapNotNull { genre ->
            val filters = buildGenreFilters(source, genre) ?: return@mapNotNull null
            val page = runCatching { source.getSearchManga(1, "", filters) }.getOrNull()
                ?: return@mapNotNull null

            val mangas = page.mangas
                .filter { it.title.isNotBlank() }
                .filter { showNsfw || !hasAdultGenre(it) }
                .filter { seen.add(it.url) }
                .take(GENRE_SAMPLE_COUNT)
                .map { it.toDomainManga(source.id) }
            if (mangas.isEmpty()) return@mapNotNull null

            val resolved = runCatching { networkToLocalManga(mangas) }.getOrDefault(emptyList())
            if (resolved.isEmpty()) return@mapNotNull null

            HomeGenreSample(
                genre = genre,
                items = resolved.map { manga ->
                    HomeHeroItem(
                        mangaId = manga.id,
                        title = manga.title,
                        coverData = manga.asMangaCover(),
                        subtitle = heroSubtitle(manga, source.name),
                    )
                },
            )
        }
    }

    /** Aplica el genero sobre una copia nueva de los filtros, igual que la pantalla de fuentes. */
    private fun buildGenreFilters(source: Source, genreName: String): FilterList? {
        val filters = source.getFilterList()

        for (sourceFilter in filters) {
            if (sourceFilter is Filter.Group<*>) {
                for (filter in sourceFilter.state) {
                    val child = filter as? Filter<*> ?: continue
                    if (child.name.equals(genreName, ignoreCase = true)) {
                        when (child) {
                            is Filter.TriState -> child.state = 1
                            is Filter.CheckBox -> child.state = true
                            else -> {}
                        }
                        return filters
                    }
                }
            } else if (sourceFilter is Filter.Select<*>) {
                val index = sourceFilter.values.filterIsInstance<String>()
                    .indexOfFirst { it.equals(genreName, ignoreCase = true) }
                if (index != -1) {
                    sourceFilter.state = index
                    return filters
                }
            }
        }

        return null
    }

    private fun hasAdultGenre(manga: SManga): Boolean {
        return manga.getGenres().orEmpty().any { genre ->
            ADULT_GENRES.any { it.equals(genre.trim(), ignoreCase = true) }
        }
    }

    private suspend fun mostUsedOnlineSource(): Source? {
        return sourceRepository.getSourcesWithFavoriteCount()
            .first()
            .sortedByDescending { it.second }
            .mapNotNull { (source, count) -> if (count > 0) sourceManager.get(source.id) else null }
            .firstOrNull { it is HttpSource }
    }

    private fun genreNames(source: Source): List<String> {
        val names = mutableListOf<String>()
        source.getFilterList().forEach { filter ->
            when (filter) {
                is Filter.Group<*> -> filter.state.forEach { child ->
                    (child as? Filter<*>)?.name?.takeIf { it.isNotBlank() }?.let(names::add)
                }

                is Filter.Select<*> -> filter.values.drop(1)
                    .filterIsInstance<String>()
                    .forEach { if (it.isNotBlank()) names.add(it) }

                else -> {}
            }
        }
        return names.distinct().take(SOURCE_GENRE_COUNT)
    }

    private fun heroSubtitle(manga: Manga, sourceName: String): String {
        return manga.genre.orEmpty()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .take(2)
            .joinToString(" · ")
            .ifBlank { sourceName }
    }

    private fun hasAdultGenre(manga: Manga): Boolean {
        return manga.genre.orEmpty().any { genre -> ADULT_GENRES.any { it.equals(genre.trim(), ignoreCase = true) } }
    }

    suspend fun getNextChapter(mangaId: Long, chapterId: Long): Chapter? {
        return getNextChapters.await(mangaId, chapterId, onlyUnread = false).firstOrNull()
    }

    private companion object {
        const val HERO_ITEM_COUNT = 6
        const val SOURCE_GENRE_COUNT = 12
        const val GENRE_SAMPLE_LIMIT = 6
        const val GENRE_SAMPLE_COUNT = 3
        val HERO_CACHE_TTL = 30.minutes
    }
}

data class HomeHistoryItem(
    val history: HistoryWithRelations,
    val progress: Float?,
    val sourceId: Long,
    val favorite: Boolean,
)

data class HomeHeroItem(
    val mangaId: Long,
    val title: String,
    val coverData: MangaCover,
    val subtitle: String,
)

data class HomeHeroState(
    val items: List<HomeHeroItem> = emptyList(),
    val sourceId: Long? = null,
    val sourceLabel: String = "",
    val sourceGenres: List<String> = emptyList(),
)

/** Un genero de la fuente con sus portadas de ejemplo. */
data class HomeGenreSample(
    val genre: String,
    val items: List<HomeHeroItem>,
)

private val ADULT_GENRES = setOf("Adult", "Ecchi", "Smut", "Hentai", "Doujinshi", "18+")
