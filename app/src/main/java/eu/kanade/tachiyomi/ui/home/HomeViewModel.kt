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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
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
        getHistory.subscribe(""),
        getLibraryManga.subscribe(),
    ) { historyList, libraryList ->
        val libraryById = libraryList.associateBy { it.manga.id }
        historyList.asSequence()
            .filter { it.mangaId in libraryById }
            .distinctBy { it.mangaId }
            .take(HISTORY_ITEM_LIMIT)
            .map { hist ->
                val libManga = libraryById.getValue(hist.mangaId)
                val progress = if (libManga.totalChapters > 0) {
                    (libManga.readCount.toFloat() / libManga.totalChapters.toFloat()).coerceIn(0f, 1f)
                } else {
                    null
                }

                HomeHistoryItem(
                    history = hist,
                    progress = progress,
                    sourceId = libManga.manga.source,
                    favorite = true,
                )
            }
            .toList()
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
    private var heroAttempts = 0

    init {
        // El banner y los generos dependen de las fuentes que tengan mangas guardados. Al
        // instalar una fuente y guardar una obra, la biblioteca cambia y aqui se recargan sin
        // tener que cerrar la app.
        viewModelScope.launch {
            getLibraryManga.subscribe()
                .map { library ->
                    library.asSequence()
                        .map { it.manga.source }
                        .distinct()
                        .sorted()
                        .toList()
                }
                .distinctUntilChanged()
                .collect { sources ->
                    if (sources.isEmpty()) return@collect
                    if (_hero.value.items.isEmpty()) {
                        delay(HOME_RETRY_DELAY_MILLIS)
                        refreshHero(force = true)
                    }
                    if (_genreSamples.value.isEmpty()) refreshGenreSamples(force = true)
                }
        }
    }

    fun refreshHero(force: Boolean = false) {
        val loadedAt = heroLoadedAt
        if (!force && loadedAt != null && Clock.System.now() - loadedAt < HERO_CACHE_TTL) return
        if (heroLoading) return
        heroLoading = true
        viewModelScope.launch {
            val loaded = runCatching { loadHero() }.getOrNull()
            // Un Inicio sin banner no se memoriza: si la fuente tardo o fallo, se reintenta en
            // vez de quedarse vacio hasta reiniciar la app.
            if (loaded != null && loaded.items.isNotEmpty()) {
                _hero.value = loaded
                heroLoadedAt = Clock.System.now()
                heroAttempts = 0
                heroLoading = false
                return@launch
            }
            heroLoading = false
            if (heroAttempts < HERO_MAX_ATTEMPTS) {
                heroAttempts++
                delay(HERO_RETRY_MILLIS)
                refreshHero(force = true)
            }
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

        // Los generos se piden a la vez: en serie el Inicio tardaba 10-15 s en mostrar portadas.
        val samples = coroutineScope {
            genres.take(GENRE_SAMPLE_LIMIT)
                .map { genre -> async { loadGenreSample(source, genre, showNsfw) } }
                .awaitAll()
        }

        val seen = mutableSetOf<Long>()
        samples.filterNotNull().mapNotNull { sample ->
            val items = sample.items.filter { seen.add(it.mangaId) }
            if (items.isEmpty()) null else sample.copy(items = items)
        }
    }

    private suspend fun loadGenreSample(
        source: Source,
        genre: String,
        showNsfw: Boolean,
    ): HomeGenreSample? {
        val filters = buildGenreFilters(source, genre) ?: return null
        val page = runCatching { source.getSearchManga(1, "", filters) }.getOrNull() ?: return null

        val mangas = page.mangas
            .filter { it.title.isNotBlank() }
            .filter { showNsfw || !hasAdultGenre(it) }
            .take(GENRE_SAMPLE_COUNT)
            .map { it.toDomainManga(source.id) }
        if (mangas.isEmpty()) return null

        val resolved = runCatching { networkToLocalManga(mangas) }.getOrDefault(emptyList())
        if (resolved.isEmpty()) return null

        return HomeGenreSample(
            genre = genre,
            items = resolved.map { manga ->
                HomeHeroItem(
                    mangaId = manga.id,
                    title = manga.title,
                    coverData = manga.asMangaCover(),
                )
            },
        )
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

    private fun hasAdultGenre(manga: Manga): Boolean {
        return manga.genre.orEmpty().any { genre -> ADULT_GENRES.any { it.equals(genre.trim(), ignoreCase = true) } }
    }

    suspend fun getNextChapter(mangaId: Long, chapterId: Long): Chapter? {
        return getNextChapters.await(mangaId, chapterId, onlyUnread = false).firstOrNull()
    }

    companion object {
        const val HERO_ITEM_COUNT = 6
        const val SOURCE_GENRE_COUNT = 12
        const val GENRE_SAMPLE_LIMIT = 6
        const val GENRE_SAMPLE_COUNT = 3
        const val HISTORY_ITEM_LIMIT = 5
        const val HOME_RETRY_DELAY_MILLIS = 1500L
        const val HERO_MAX_ATTEMPTS = 2
        const val HERO_RETRY_MILLIS = 6000L
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
