package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AlarmSoundType
import com.example.data.model.PrayerName
import com.example.data.model.PrayerTimeItem
import com.example.ui.MainUiState
import com.example.ui.MainViewModel
import com.example.ui.components.CityPickerSheet
import com.example.ui.components.MonthlyScheduleDialog
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerHomeScreen(
    viewModel: MainViewModel,
    uiState: MainUiState,
    onRequestLocationPermission: () -> Unit
) {
    var showCityPicker by remember { mutableStateOf(false) }
    var showMonthlySchedule by remember { mutableStateOf(false) }

    val schedule = uiState.todaySchedule
    val countdown = uiState.countdown
    val timeFormat = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val dateFormat = remember { DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale("id", "ID")) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("prayer_home_screen"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Top Location and Date Bar
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Location Pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .clickable { showCityPicker = true }
                            .testTag("location_picker_chip")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (uiState.userLocation.isAutoDetected) Icons.Default.MyLocation else Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = uiState.userLocation.cityName,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Monthly Schedule Action
                    IconButton(
                        onClick = { showMonthlySchedule = true },
                        modifier = Modifier.testTag("monthly_schedule_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Jadwal Bulanan",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Hijri & Gregorian Date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = uiState.selectedDate.format(dateFormat),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = uiState.hijriDate.formatted,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Hero Card with Mosque Banner & Dynamic Next Prayer Countdown
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("prayer_hero_card"),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                ) {
                    // Hero Image Background
                    Image(
                        painter = painterResource(id = R.drawable.hero_islamic_bg),
                        contentDescription = "Islamic Mosque Archway",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Gradient Scrim for readable text
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x70000000),
                                        Color(0xCC062E1F)
                                    )
                                )
                            )
                    )

                    // Hero Content
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top Label
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0x33FFFFFF)
                            ) {
                                Text(
                                    text = if (countdown != null) "Menuju ${countdown.nextPrayer.displayName}" else "Waktu Sholat",
                                    color = Color(0xFFFDE047),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            // Test Adhan Audio Button
                            FilledTonalButton(
                                onClick = { viewModel.toggleAdhanPreview() },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (uiState.isPlayingAdhanPreview) Color(0xFFEF4444) else Color(0x40FFFFFF),
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.testTag("test_adhan_button")
                            ) {
                                Icon(
                                    imageVector = if (uiState.isPlayingAdhanPreview) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (uiState.isPlayingAdhanPreview) "Hentikan" else "Uji Azan",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Middle: Countdown Clock
                        if (countdown != null) {
                            Column {
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = String.format("%02d:%02d:%02d", countdown.hoursRemaining, countdown.minutesRemaining, countdown.secondsRemaining),
                                        fontSize = 38.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "(${countdown.time.format(timeFormat)})",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFFD1FAE5),
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                }
                                Text(
                                    text = "Sholat ${countdown.nextPrayer.displayName} - ${countdown.nextPrayer.arabicName}",
                                    fontSize = 13.sp,
                                    color = Color(0xFFA7F3D0)
                                )
                            }
                        }

                        // Bottom: Linear Countdown Progress
                        if (countdown != null) {
                            Column {
                                LinearProgressIndicator(
                                    progress = { countdown.progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = Color(0xFF10B981),
                                    trackColor = Color(0x33FFFFFF)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section Title: Jadwal Hari Ini
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Jadwal Sholat 5 Waktu",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Pengingat Azan",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // List of Prayers
        if (schedule != null) {
            val prayerItems = schedule.toList()
            items(prayerItems) { item ->
                PrayerTimeCard(
                    item = item,
                    timeFormat = timeFormat,
                    isAlarmEnabled = viewModel.isPrayerAlarmEnabled(item.prayer),
                    alarmSound = viewModel.getPrayerAlarmSound(item.prayer),
                    onToggleAlarm = { viewModel.togglePrayerAlarm(item.prayer) },
                    onChangeSound = { viewModel.setPrayerAlarmSound(item.prayer, it) }
                )
            }
        }

        // Quick Widget Advice Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Widgets,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Widget Layar Utama Tersedia",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Pasang widget Jadwal Sholat di beranda Android Anda untuk akses waktu sholat lebih cepat.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (showCityPicker) {
        CityPickerSheet(
            onDismiss = { showCityPicker = false },
            onSelectCity = { viewModel.selectCity(it) },
            onRequestGps = { onRequestLocationPermission() }
        )
    }

    if (showMonthlySchedule) {
        MonthlyScheduleDialog(
            userLocation = uiState.userLocation,
            calculationMethod = uiState.calculationMethod,
            ihtiyatMinutes = uiState.ihtiyatMinutes,
            onDismiss = { showMonthlySchedule = false }
        )
    }
}

@Composable
fun PrayerTimeCard(
    item: PrayerTimeItem,
    timeFormat: DateTimeFormatter,
    isAlarmEnabled: Boolean,
    alarmSound: AlarmSoundType,
    onToggleAlarm: () -> Unit,
    onChangeSound: (AlarmSoundType) -> Unit
) {
    var showSoundMenu by remember { mutableStateOf(false) }

    val isNext = item.isNext
    val cardBg = if (isNext) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .testTag("prayer_card_${item.prayer.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = if (isNext) CardDefaults.outlinedCardBorder() else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Prayer Name & Arabic
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isNext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getPrayerIcon(item.prayer),
                        contentDescription = null,
                        tint = if (isNext) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.prayer.displayName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isNext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        if (isNext) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = "Selanjutnya",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = item.prayer.arabicName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Prayer Time & Alarm Toggle
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.time.format(timeFormat),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = if (isNext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )

                if (item.prayer.isFardh) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Box {
                        IconButton(
                            onClick = { showSoundMenu = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = when {
                                    !isAlarmEnabled || alarmSound == AlarmSoundType.SILENT -> Icons.Default.NotificationsOff
                                    alarmSound == AlarmSoundType.AZAN -> Icons.Default.NotificationsActive
                                    alarmSound == AlarmSoundType.VIBRATE -> Icons.Default.Vibration
                                    else -> Icons.Default.Notifications
                                },
                                contentDescription = "Pengaturan Suara",
                                tint = if (isAlarmEnabled && alarmSound != AlarmSoundType.SILENT) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outline
                                }
                            )
                        }

                        DropdownMenu(
                            expanded = showSoundMenu,
                            onDismissRequest = { showSoundMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Azan Lengkap") },
                                onClick = {
                                    onChangeSound(AlarmSoundType.AZAN)
                                    showSoundMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.VolumeUp, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Nada Notifikasi") },
                                onClick = {
                                    onChangeSound(AlarmSoundType.BEEP)
                                    showSoundMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.Notifications, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Getar Saja") },
                                onClick = {
                                    onChangeSound(AlarmSoundType.VIBRATE)
                                    showSoundMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.Vibration, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Matikan Alarm") },
                                onClick = {
                                    onChangeSound(AlarmSoundType.SILENT)
                                    showSoundMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.NotificationsOff, null) }
                            )
                        }
                    }
                }
            }
        }
    }
}

fun getPrayerIcon(prayer: PrayerName) = when (prayer) {
    PrayerName.IMSAK -> Icons.Default.NightsStay
    PrayerName.SUBUH -> Icons.Default.Brightness3
    PrayerName.TERBIT -> Icons.Default.WbTwilight
    PrayerName.DHUHA -> Icons.Default.WbSunny
    PrayerName.DZUHUR -> Icons.Default.Brightness7
    PrayerName.ASHAR -> Icons.Default.Brightness6
    PrayerName.MAGHRIB -> Icons.Default.Brightness4
    PrayerName.ISYA -> Icons.Default.Bedtime
}
