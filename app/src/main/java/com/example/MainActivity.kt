package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppNavTab
import com.example.ui.MainViewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val bookmarks by viewModel.allBookmarks.collectAsStateWithLifecycle()
    val lastRead by viewModel.lastRead.collectAsStateWithLifecycle()

    // Permission launcher for Location
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            viewModel.requestAutoLocation()
        }
    }

    // Permission launcher for Notifications (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    val requestLocationPermission = {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // If a Surah is opened, show SurahDetailScreen full screen with its own scaffold
    if (uiState.selectedSurah != null) {
        SurahDetailScreen(
            surah = uiState.selectedSurah!!,
            verses = uiState.currentVerses,
            viewModel = viewModel,
            uiState = uiState,
            bookmarks = bookmarks,
            onBack = { viewModel.closeSurahDetail() }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                NavigationBar(
                    windowInsets = WindowInsets.navigationBars,
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    NavigationBarItem(
                        selected = uiState.currentTab == AppNavTab.JADWAL,
                        onClick = { viewModel.selectTab(AppNavTab.JADWAL) },
                        icon = {
                            Icon(
                                imageVector = if (uiState.currentTab == AppNavTab.JADWAL) Icons.Filled.AccessTime else Icons.Outlined.AccessTime,
                                contentDescription = "Jadwal"
                            )
                        },
                        label = { Text("Jadwal") },
                        modifier = Modifier.testTag("nav_tab_jadwal")
                    )

                    NavigationBarItem(
                        selected = uiState.currentTab == AppNavTab.KIBLAT,
                        onClick = { viewModel.selectTab(AppNavTab.KIBLAT) },
                        icon = {
                            Icon(
                                imageVector = if (uiState.currentTab == AppNavTab.KIBLAT) Icons.Filled.Explore else Icons.Outlined.Explore,
                                contentDescription = "Kiblat"
                            )
                        },
                        label = { Text("Kiblat") },
                        modifier = Modifier.testTag("nav_tab_kiblat")
                    )

                    NavigationBarItem(
                        selected = uiState.currentTab == AppNavTab.QURAN,
                        onClick = { viewModel.selectTab(AppNavTab.QURAN) },
                        icon = {
                            Icon(
                                imageVector = if (uiState.currentTab == AppNavTab.QURAN) Icons.Filled.AutoStories else Icons.Outlined.AutoStories,
                                contentDescription = "Al-Qur'an"
                            )
                        },
                        label = { Text("Al-Qur'an") },
                        modifier = Modifier.testTag("nav_tab_quran")
                    )

                    NavigationBarItem(
                        selected = uiState.currentTab == AppNavTab.DOA,
                        onClick = { viewModel.selectTab(AppNavTab.DOA) },
                        icon = {
                            Icon(
                                imageVector = if (uiState.currentTab == AppNavTab.DOA) Icons.Filled.VolunteerActivism else Icons.Outlined.VolunteerActivism,
                                contentDescription = "Doa & Dzikir"
                            )
                        },
                        label = { Text("Doa") },
                        modifier = Modifier.testTag("nav_tab_doa")
                    )

                    NavigationBarItem(
                        selected = uiState.currentTab == AppNavTab.SETTINGS,
                        onClick = { viewModel.selectTab(AppNavTab.SETTINGS) },
                        icon = {
                            Icon(
                                imageVector = if (uiState.currentTab == AppNavTab.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                                contentDescription = "Pengaturan"
                            )
                        },
                        label = { Text("Pengaturan") },
                        modifier = Modifier.testTag("nav_tab_settings")
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (uiState.currentTab) {
                    AppNavTab.JADWAL -> PrayerHomeScreen(
                        viewModel = viewModel,
                        uiState = uiState,
                        onRequestLocationPermission = requestLocationPermission
                    )
                    AppNavTab.KIBLAT -> QiblaScreen(
                        viewModel = viewModel,
                        uiState = uiState,
                        onRequestLocationPermission = requestLocationPermission
                    )
                    AppNavTab.QURAN -> QuranScreen(
                        viewModel = viewModel,
                        uiState = uiState,
                        bookmarks = bookmarks,
                        lastRead = lastRead,
                        onOpenSurah = { viewModel.openSurah(it) }
                    )
                    AppNavTab.DOA -> DoaAndTasbihScreen(
                        viewModel = viewModel,
                        uiState = uiState
                    )
                    AppNavTab.SETTINGS -> SettingsScreen(
                        viewModel = viewModel,
                        uiState = uiState
                    )
                }
            }
        }
    }
}
