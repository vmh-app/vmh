package eu.kanade.tachiyomi.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.TabOptions
import dev.zacsweers.metrox.viewmodel.metroViewModel
import eu.kanade.presentation.home.HomeScreen
import eu.kanade.presentation.util.Tab
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.ui.browse.source.browse.BrowseSourceScreen
import eu.kanade.tachiyomi.ui.manga.MangaScreen
import eu.kanade.tachiyomi.ui.reader.ReaderActivity
import eu.kanade.tachiyomi.ui.updates.UpdatesTab
import eu.kanade.tachiyomi.util.system.toast
import kotlinx.coroutines.launch
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

data object HomeTab : Tab {

    override val options: TabOptions
        @Composable
        get() = TabOptions(
            index = 0u,
            title = stringResource(MR.strings.label_home),
            icon = painterResource(R.drawable.ic_vmh_nav_home),
        )

    override suspend fun onReselect(navigator: Navigator) {
        // En futuras iteraciones: hacer scroll al principio de la vista
    }

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val viewModel = metroViewModel<HomeViewModel>()

        val hero by viewModel.hero.collectAsStateWithLifecycle()
        val updates by viewModel.updates.collectAsStateWithLifecycle()
        val history by viewModel.history.collectAsStateWithLifecycle()
        val genreSamples by viewModel.genreSamples.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) {
            viewModel.refreshHero()
            viewModel.refreshGenreSamples()
        }

        HomeScreen(
            hero = hero,
            updates = updates,
            history = history,
            genreSamples = genreSamples,
            onMangaClick = { navigator.push(MangaScreen(it)) },
            onHistoryContinue = { mangaId, chapterId ->
                scope.launch {
                    val chapter = viewModel.getNextChapter(mangaId, chapterId)
                    if (chapter != null) {
                        val intent = ReaderActivity.newIntent(context, chapter.mangaId, chapter.id)
                        context.startActivity(intent)
                    } else {
                        context.toast(MR.strings.no_next_chapter)
                    }
                }
            },
            onViewAllUpdates = { navigator.push(UpdatesTab) },
            onSourceGenreClick = { genre ->
                hero.sourceId?.let { navigator.push(BrowseSourceScreen(it, null, genre)) }
            },
            onSampleMangaClick = { navigator.push(MangaScreen(it, fromSource = true)) },
        )
    }
}
