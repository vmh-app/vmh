package eu.kanade.presentation.home

import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import eu.kanade.presentation.library.components.MangaCompactGridItem
import eu.kanade.presentation.library.components.MangaListItem
import eu.kanade.presentation.util.relativeTimeSpanString
import eu.kanade.tachiyomi.ui.home.HomeGenreSample
import eu.kanade.tachiyomi.ui.home.HomeHeroItem
import eu.kanade.tachiyomi.ui.home.HomeHeroState
import eu.kanade.tachiyomi.ui.home.HomeHistoryItem
import kotlinx.coroutines.delay
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.automirroredrounded.ArrowForward
import tachiyomi.domain.updates.model.UpdatesWithRelations
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.VmhCoverShape
import tachiyomi.presentation.core.components.VmhPillShape
import tachiyomi.presentation.core.components.VmhScreenHeader
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.screens.EmptyArt
import tachiyomi.presentation.core.screens.EmptyMascot
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds

@Composable
fun HomeScreen(
    hero: HomeHeroState,
    updates: List<UpdatesWithRelations>,
    history: List<HomeHistoryItem>,
    genreSamples: List<HomeGenreSample>,
    onMangaClick: (Long) -> Unit,
    onHistoryContinue: (mangaId: Long, chapterId: Long) -> Unit,
    onViewAllUpdates: () -> Unit,
    onSourceGenreClick: (String) -> Unit,
    onSampleMangaClick: (Long) -> Unit,
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .statusBarsPadding()
                    .padding(start = 18.dp, end = 16.dp, top = 14.dp, bottom = 6.dp),
            ) {
                VmhScreenHeader(
                    eyebrow = stringResource(MR.strings.app_name),
                    title = stringResource(MR.strings.label_home),
                )
            }
        },
    ) { paddingValues ->
        if (updates.isEmpty() && history.isEmpty() && genreSamples.isEmpty() && hero.items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    EmptyMascot(art = EmptyArt.SLEEP, modifier = Modifier.fillMaxWidth())
                    Text(
                        text = stringResource(MR.strings.home_empty),
                        modifier = Modifier.padding(top = 18.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(paddingValues)
                    .padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                if (hero.items.isNotEmpty()) {
                    HeroBanner(state = hero, onMangaClick = onMangaClick)
                }

                if (updates.isNotEmpty()) {
                    UpdatesSection(
                        updates = updates,
                        onViewAllClick = onViewAllUpdates,
                        onMangaClick = onMangaClick,
                    )
                }

                if (history.isNotEmpty()) {
                    HistorySection(
                        history = history,
                        onMangaClick = onMangaClick,
                        onContinue = onHistoryContinue,
                    )
                }

                if (hero.sourceGenres.isNotEmpty()) {
                    GenresSection(
                        sourceLabel = hero.sourceLabel,
                        sourceGenres = hero.sourceGenres,
                        samples = genreSamples,
                        onGenreClick = onSourceGenreClick,
                        onSampleMangaClick = onSampleMangaClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroBanner(
    state: HomeHeroState,
    onMangaClick: (Long) -> Unit,
) {
    val items = state.items
    if (items.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { items.size })

    LaunchedEffect(pagerState, items.size) {
        if (items.size < 2) return@LaunchedEffect
        while (true) {
            delay(HERO_ADVANCE_MILLIS)
            if (!pagerState.isScrollInProgress) {
                val next = (pagerState.currentPage + 1) % items.size
                if (next == 0) {
                    pagerState.scrollToPage(0)
                } else {
                    pagerState.animateScrollToPage(
                        page = next,
                        animationSpec = tween(durationMillis = HERO_SLIDE_MILLIS),
                    )
                }
            }
        }
    }

    HorizontalPager(state = pagerState) { page ->
        HeroCard(
            item = items[page],
            modifier = Modifier.padding(horizontal = 16.dp),
            sourceLabel = state.sourceLabel,
            currentPage = pagerState.currentPage,
            pageCount = items.size,
            onClick = { onMangaClick(items[page].mangaId) },
        )
    }
}

@Composable
private fun HeroCard(
    item: HomeHeroItem,
    sourceLabel: String,
    currentPage: Int,
    pageCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hero = rememberVmhHeroPalette()

    val cardShape = RoundedCornerShape(26.dp)
    val coverShape = RoundedCornerShape(16.dp)
    val halo = hero.brillo.copy(alpha = 0.45f)

    Surface(
        onClick = onClick,
        shape = cardShape,
        color = hero.fondoAbajo,
        border = BorderStroke(1.dp, hero.borde),
        shadowElevation = 0.dp,
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 18.dp,
                shape = cardShape,
                ambientColor = halo,
                spotColor = halo,
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(HERO_HEIGHT),
        ) {
            // El fondo es la propia portada, desvanecida bajo un velo de tinta: la ficha muestra
            // la obra en vez de una tarjeta de color plana, sin escalarla ni pixelarla.
            AsyncImage(
                model = item.coverData,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .drawBehind {
                        val w = size.width
                        val h = size.height
                        drawRect(
                            brush = Brush.linearGradient(
                                colorStops = arrayOf(
                                    0f to hero.fondoAbajo.copy(alpha = 0.30f),
                                    0.55f to hero.fondoAbajo.copy(alpha = 0.48f),
                                    1f to hero.fondoAbajo.copy(alpha = 0.62f),
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(w, h),
                            ),
                        )
                        drawRect(
                            brush = Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0f to Color.Transparent,
                                    0.45f to Color.Transparent,
                                    1f to hero.fondoAbajo.copy(alpha = 0.38f),
                                ),
                            ),
                        )
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(hero.brillo.copy(alpha = 0.30f), Color.Transparent),
                                center = Offset(w, 0f),
                                radius = w * 0.85f,
                            ),
                        )
                        val radio = 26.dp.toPx()
                        drawLine(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    hero.luzSuperior,
                                    Color.Transparent,
                                ),
                            ),
                            start = Offset(radio, 0.5f),
                            end = Offset(w - radio, 0.5f),
                            strokeWidth = 1.dp.toPx(),
                        )
                    },
            )
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .width(112.dp)
                        .height(172.dp)
                        .drawBehind {
                            // Halo detras de la portada: la despega de la tinta.
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(hero.brillo.copy(alpha = 0.50f), Color.Transparent),
                                    center = center,
                                    radius = size.width * 0.95f,
                                ),
                                radius = size.width * 0.95f,
                                center = center,
                            )
                        },
                ) {
                    AsyncImage(
                        model = item.coverData,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .shadow(
                                elevation = 18.dp,
                                shape = coverShape,
                                ambientColor = halo,
                                spotColor = halo,
                            )
                            .clip(coverShape)
                            .background(hero.fondoArriba)
                            .border(1.5.dp, hero.chipBorde, coverShape),
                    )
                }

                Spacer(modifier = Modifier.width(18.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .width(16.dp)
                                .height(2.dp)
                                .background(hero.acento, RoundedCornerShape(2.dp)),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(MR.strings.home_featured_today).uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.2.sp),
                            fontWeight = FontWeight.Bold,
                            color = hero.acento,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            shadow = Shadow(hero.fondoAbajo, Offset(0f, 2f), 8f),
                        ),
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 28.sp,
                        color = hero.texto,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = item.subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            shadow = Shadow(hero.fondoAbajo, Offset(0f, 1f), 6f),
                        ),
                        color = hero.textoSuave,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (sourceLabel.isNotBlank()) {
                            Surface(
                                modifier = Modifier.widthIn(max = 168.dp),
                                shape = CircleShape,
                                color = hero.chipFondo,
                                border = BorderStroke(1.dp, hero.chipBorde),
                            ) {
                                Text(
                                    text = sourceLabel,
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = hero.texto,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "${currentPage + 1} / $pageCount",
                            style = MaterialTheme.typography.labelMedium.copy(
                                shadow = Shadow(hero.fondoAbajo, Offset(0f, 1f), 6f),
                            ),
                            fontWeight = FontWeight.SemiBold,
                            color = hero.textoSuave,
                            maxLines = 1,
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(
                            modifier = Modifier
                                .requiredSize(40.dp)
                                .shadow(
                                    elevation = 6.dp,
                                    shape = CircleShape,
                                    ambientColor = hero.brilloCian.copy(alpha = 0.40f),
                                    spotColor = hero.brilloCian.copy(alpha = 0.40f),
                                )
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(hero.flechaInicio, hero.flechaFin),
                                    ),
                                    shape = CircleShape,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = MaterialSymbols.AutoMirroredRounded.ArrowForward,
                                contentDescription = null,
                                tint = hero.flechaIcono,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, action: (@Composable () -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title.uppercase(),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
        )
        action?.invoke()
    }
}

@Composable
private fun UpdatesSection(
    updates: List<UpdatesWithRelations>,
    onViewAllClick: () -> Unit,
    onMangaClick: (Long) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader(
            title = stringResource(MR.strings.home_latest_updates),
            action = {
                TextButton(onClick = onViewAllClick) {
                    Text(stringResource(MR.strings.home_view_all))
                }
            },
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(updates, key = { it.mangaId }) { item ->
                Column(modifier = Modifier.width(124.dp)) {
                    MangaCompactGridItem(
                        coverData = item.coverData,
                        title = item.mangaTitle,
                        onClick = { onMangaClick(item.mangaId) },
                        onLongClick = { },
                        coverBadgeStart = {
                            if (Clock.System.now() - item.dateFetch < NEW_CHAPTER_WINDOW) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                ) {
                                    Text(
                                        text = stringResource(MR.strings.home_update_new).uppercase(),
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        maxLines = 1,
                                    )
                                }
                            }
                        },
                    )
                    Text(
                        text = item.chapterName,
                        modifier = Modifier
                            .padding(start = 2.dp, top = 6.dp, end = 2.dp)
                            .clip(VmhPillShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = relativeTimeSpanString(item.dateFetch.toEpochMilliseconds()),
                        modifier = Modifier.padding(start = 4.dp, top = 3.dp, end = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun HistorySection(
    history: List<HomeHistoryItem>,
    onMangaClick: (Long) -> Unit,
    onContinue: (mangaId: Long, chapterId: Long) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader(title = stringResource(MR.strings.home_recently_read))
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            history.forEach { item ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                ) {
                    MangaListItem(
                        coverData = item.history.coverData,
                        title = item.history.title,
                        onClick = { onMangaClick(item.history.mangaId) },
                        onLongClick = { },
                        badge = { },
                        progress = item.progress,
                        onClickContinueReading = { onContinue(item.history.mangaId, item.history.chapterId) },
                    )
                }
            }
        }
    }
}

@Composable
private fun GenresSection(
    sourceLabel: String,
    sourceGenres: List<String>,
    samples: List<HomeGenreSample>,
    onGenreClick: (String) -> Unit,
    onSampleMangaClick: (Long) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader(title = stringResource(MR.strings.home_genres))
        if (sourceLabel.isNotBlank()) {
            Text(
                text = sourceLabel,
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (samples.isEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(sourceGenres, key = { it }) { genre ->
                    SuggestionChip(
                        onClick = { onGenreClick(genre) },
                        label = { Text(genre) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                        border = null,
                        shape = RoundedCornerShape(16.dp),
                    )
                }
            }
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(samples, key = { it.genre }) { sample ->
                    GenreSampleCard(
                        sample = sample,
                        onClick = { onGenreClick(sample.genre) },
                        onSampleMangaClick = onSampleMangaClick,
                    )
                }
            }
        }
    }
}

/**
 * Ficha de genero: el nombre abre el genero en la fuente y debajo van tres portadas de ejemplo,
 * elegidas para no repetirse entre generos.
 */
@Composable
private fun GenreSampleCard(
    sample: HomeGenreSample,
    onClick: () -> Unit,
    onSampleMangaClick: (Long) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = colors.surfaceContainer,
        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.7f)),
        modifier = Modifier.width(GENRE_CARD_WIDTH),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = sample.genre,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.width(6.dp))
                Icon(
                    imageVector = MaterialSymbols.AutoMirroredRounded.ArrowForward,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(16.dp),
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                sample.items.forEach { item ->
                    AsyncImage(
                        model = item.coverData,
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .width(GENRE_SAMPLE_COVER_WIDTH)
                            .height(GENRE_SAMPLE_COVER_HEIGHT)
                            .border(1.dp, colors.outlineVariant.copy(alpha = 0.55f), VmhCoverShape)
                            .clip(VmhCoverShape)
                            .clickable { onSampleMangaClick(item.mangaId) },
                    )
                }
            }
        }
    }
}

private val HERO_HEIGHT = 236.dp

private val GENRE_CARD_WIDTH = 236.dp

private val GENRE_SAMPLE_COVER_WIDTH = 66.dp

private val GENRE_SAMPLE_COVER_HEIGHT = 99.dp

private const val HERO_SLIDE_MILLIS = 700

private const val HERO_ADVANCE_MILLIS = 5000L

private val NEW_CHAPTER_WINDOW = 3.days
