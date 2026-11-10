package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.BookmarkEntity
import com.example.data.quran.Surah
import com.example.data.quran.Verse
import com.example.ui.MainUiState
import com.example.ui.MainViewModel
import com.example.ui.components.TajwidArabicText
import com.example.ui.components.TajwidLegendSheet
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahDetailScreen(
    surah: Surah,
    verses: List<Verse>,
    viewModel: MainViewModel,
    uiState: MainUiState,
    bookmarks: List<BookmarkEntity>,
    onBack: () -> Unit
) {
    BackHandler {
        viewModel.stopVerseAudio()
        onBack()
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var showTajwidLegend by remember { mutableStateOf(false) }
    var showFontSizeSlider by remember { mutableStateOf(false) }

    val playingVerse = uiState.playingVerseNumber
    val isAudioPlaying = uiState.isQuranPlaying

    // Scroll to active playing verse
    LaunchedEffect(playingVerse) {
        if (playingVerse != null && playingVerse in 1..verses.size) {
            val index = verses.indexOfFirst { it.verseNumber == playingVerse }
            if (index >= 0) {
                val hasBasmalah = (surah.number != 9 && surah.number != 1)
                val targetIndex = if (hasBasmalah) index + 1 else index

                val visibleItems = listState.layoutInfo.visibleItemsInfo
                val visibleItem = visibleItems.find { it.index == targetIndex }
                val viewportStart = listState.layoutInfo.viewportStartOffset
                val viewportEnd = listState.layoutInfo.viewportEndOffset

                // Check if the verse is already nicely visible in the current viewport
                val isComfortablyVisible = visibleItem != null &&
                    visibleItem.offset >= (viewportStart + 16) &&
                    (visibleItem.offset + (visibleItem.size / 3)) <= (viewportEnd - 120)

                if (!isComfortablyVisible) {
                    listState.animateScrollToItem(targetIndex, scrollOffset = 0)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = surah.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${surah.translation} • ${surah.totalVerses} Ayat",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.stopVerseAudio()
                        onBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                actions = {
                    // Tajwid Toggle Button
                    IconButton(onClick = { viewModel.toggleTajwid() }) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Toggle Tajwid",
                            tint = if (uiState.isTajwidEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }

                    // Font Size Toggle
                    IconButton(onClick = { showFontSizeSlider = !showFontSizeSlider }) {
                        Icon(
                            imageVector = Icons.Default.FormatSize,
                            contentDescription = "Ukuran Tulisan"
                        )
                    }

                    // Tajwid Guide Info
                    IconButton(onClick = { showTajwidLegend = true }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Panduan Tajwid"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            // Persistent Audio Control Bar
            AnimatedVisibility(
                visible = playingVerse != null,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 8.dp,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isAudioPlaying) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Murottal: ${surah.name} : $playingVerse",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (isAudioPlaying) "Memutar ayat..." else "Dijeda",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Playback Control Buttons
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Previous Verse
                            IconButton(
                                onClick = {
                                    val prev = (playingVerse ?: 1) - 1
                                    if (prev >= 1) viewModel.playVerseAudio(prev)
                                },
                                enabled = (playingVerse ?: 1) > 1
                            ) {
                                Icon(Icons.Default.SkipPrevious, contentDescription = "Ayat Sebelumnya")
                            }

                            // Play / Pause
                            FilledIconButton(
                                onClick = {
                                    if (playingVerse != null) {
                                        viewModel.playVerseAudio(playingVerse)
                                    }
                                },
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(
                                    imageVector = if (isAudioPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isAudioPlaying) "Jeda" else "Putar"
                                )
                            }

                            // Next Verse
                            IconButton(
                                onClick = {
                                    val next = (playingVerse ?: 1) + 1
                                    if (next <= verses.size) viewModel.playVerseAudio(next)
                                },
                                enabled = (playingVerse ?: 1) < verses.size
                            ) {
                                Icon(Icons.Default.SkipNext, contentDescription = "Ayat Selanjutnya")
                            }

                            // Close / Stop
                            IconButton(onClick = { viewModel.stopVerseAudio() }) {
                                Icon(Icons.Default.Close, contentDescription = "Tutup Pemutar")
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("surah_detail_screen")
        ) {
            // Collapsible Font Size Slider
            if (showFontSizeSlider) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Ukuran Huruf Arab", style = MaterialTheme.typography.labelMedium)
                            Text(text = "${uiState.quranFontSizeSp.toInt()} sp", fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = uiState.quranFontSizeSp,
                            onValueChange = { viewModel.setQuranFontSize(it) },
                            valueRange = 18f..38f,
                            steps = 9
                        )
                    }
                }
            }

            // Verses List or Loading
            if (verses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Memuat ayat Surah ${surah.name}...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Basmalah Header Card (not shown for Surah 9 At-Taubah, and not repeated for Surah 1)
                if (surah.number != 9 && surah.number != 1) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 18.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                items(verses, key = { it.verseNumber }) { verse ->
                    val isBookmarked = bookmarks.any { it.surahNumber == verse.surahNumber && it.verseNumber == verse.verseNumber }
                    val isPlayingThis = (playingVerse == verse.verseNumber && isAudioPlaying)
                    val isLoadedThis = (playingVerse == verse.verseNumber)

                    VerseCard(
                        verse = verse,
                        fontSizeSp = uiState.quranFontSizeSp,
                        isTajwidEnabled = uiState.isTajwidEnabled,
                        isBookmarked = isBookmarked,
                        isPlaying = isPlayingThis,
                        isLoaded = isLoadedThis,
                        onToggleBookmark = { viewModel.toggleBookmark(verse, surah.name) },
                        onTogglePlay = {
                            viewModel.playVerseAudio(verse.verseNumber)
                        },
                        onCopy = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText(
                                "Ayat Al-Qur'an",
                                "${verse.arabicText}\n\n${verse.translation} (QS. ${surah.name}: ${verse.verseNumber})"
                            )
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Ayat disalin ke clipboard", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

        if (showTajwidLegend) {
            TajwidLegendSheet(onDismiss = { showTajwidLegend = false })
        }
    }
}

@Composable
fun VerseCard(
    verse: Verse,
    fontSizeSp: Float,
    isTajwidEnabled: Boolean,
    isBookmarked: Boolean,
    isPlaying: Boolean,
    isLoaded: Boolean = false,
    onToggleBookmark: () -> Unit,
    onTogglePlay: () -> Unit,
    onCopy: () -> Unit
) {
    val cardColor = when {
        isPlaying -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
        isLoaded -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("verse_card_${verse.verseNumber}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = if (isPlaying || isLoaded) CardDefaults.outlinedCardBorder() else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Verse Action Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Verse Number Pill
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = verse.verseNumber.toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isPlaying) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Row of Tools (Play Audio, Bookmark, Copy)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onTogglePlay,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.PauseCircleFilled else Icons.Default.PlayCircle,
                            contentDescription = if (isPlaying) "Jeda Suara" else "Putar Suara Murottal",
                            tint = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onToggleBookmark, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onCopy, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Salin",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Arabic Text with Tajwid
            TajwidArabicText(
                verse = verse,
                fontSizeSp = fontSizeSp,
                isTajwidEnabled = isTajwidEnabled,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Latin Transliteration
            if (verse.transliteration.isNotBlank()) {
                Text(
                    text = verse.transliteration,
                    style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Indonesian Translation
            Text(
                text = verse.translation,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
