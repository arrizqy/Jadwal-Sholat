package com.example.ui.screens

import android.hardware.SensorManager
import java.util.Locale
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculation.QiblaCalculator
import com.example.ui.MainUiState
import com.example.ui.MainViewModel
import kotlin.math.*

@Composable
fun QiblaScreen(
    viewModel: MainViewModel,
    uiState: MainUiState,
    onRequestLocationPermission: () -> Unit
) {
    val qiblaBearing = uiState.qiblaBearing
    val currentAzimuth = uiState.compassAzimuth
    val isFacing = uiState.isFacingQibla

    // Smooth needle rotation: target angle from device top towards Qibla
    var relativeAngle = (qiblaBearing.toFloat() - currentAzimuth + 360f) % 360f

    val animatedRelativeAngle by animateFloatAsState(
        targetValue = relativeAngle,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy),
        label = "qibla_rotation"
    )

    // Pulsing green glow when facing Qibla
    val infiniteTransition = rememberInfiniteTransition(label = "facing_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val targetStatusColor = if (isFacing) Color(0xFF10B981) else MaterialTheme.colorScheme.primary
    val statusColor by animateColorAsState(targetValue = targetStatusColor, label = "status_color")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .testTag("qibla_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Location & Bearing Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${uiState.userLocation.cityName} • Kiblat: ${String.format(Locale.getDefault(), "%.1f°", qiblaBearing)} ${QiblaCalculator.getDirectionLabel(qiblaBearing)}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Facing Status Banner
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isFacing) Color(0xFF064E3B) else MaterialTheme.colorScheme.surface,
                border = if (isFacing) CardDefaults.outlinedCardBorder() else null,
                shadowElevation = if (isFacing) 6.dp else 1.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = if (isFacing) Icons.Default.CheckCircle else Icons.Default.Explore,
                        contentDescription = null,
                        tint = if (isFacing) Color(0xFF34D399) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isFacing) "Anda Tepat Menghadap Kiblat!" else "Arahkan jarum emas ke atas",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isFacing) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Compass Dial Area
        Box(
            modifier = Modifier
                .size(310.dp)
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            // Background Glow when facing
            if (isFacing) {
                Box(
                    modifier = Modifier
                        .size(300.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF10B981).copy(alpha = pulseAlpha * 0.4f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            // Dial Background with Cardinal points
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(-currentAzimuth)
            ) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.minDimension / 2 - 16.dp.toPx()

                // Outer Ring
                drawCircle(
                    color = Color(0x3310B981),
                    radius = radius,
                    center = center,
                    style = Stroke(width = 3.dp.toPx())
                )

                // Minor tick marks
                for (i in 0 until 360 step 5) {
                    val angleRad = Math.toRadians(i.toDouble() - 90.0)
                    val isMajor = i % 30 == 0
                    val isCardinal = i % 90 == 0
                    val tickLen = when {
                        isCardinal -> 14.dp.toPx()
                        isMajor -> 9.dp.toPx()
                        else -> 4.dp.toPx()
                    }
                    val tickColor = when {
                        i == 0 -> Color(0xFFEF4444) // North is Red
                        isCardinal -> Color(0xFF10B981)
                        isMajor -> Color(0xFF94A3B8)
                        else -> Color(0x3394A3B8)
                    }

                    val startX = center.x + (radius - tickLen) * cos(angleRad).toFloat()
                    val startY = center.y + (radius - tickLen) * sin(angleRad).toFloat()
                    val endX = center.x + radius * cos(angleRad).toFloat()
                    val endY = center.y + radius * sin(angleRad).toFloat()

                    drawLine(
                        color = tickColor,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = if (isCardinal) 3.dp.toPx() else 1.5.dp.toPx()
                    )
                }
            }

            // Fixed Center Indicator & Rotating Qibla Needle
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(animatedRelativeAngle)
            ) {
                val center = Offset(size.width / 2, size.height / 2)
                val needleLength = size.minDimension / 2 - 32.dp.toPx()

                // Golden Kaaba Arrow pointing towards Qibla
                val arrowPath = Path().apply {
                    moveTo(center.x, center.y - needleLength)
                    lineTo(center.x - 14.dp.toPx(), center.y - 20.dp.toPx())
                    lineTo(center.x, center.y)
                    lineTo(center.x + 14.dp.toPx(), center.y - 20.dp.toPx())
                    close()
                }

                drawPath(
                    path = arrowPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF59E0B),
                            Color(0xFFD97706)
                        )
                    )
                )

                // Opposite Needle (Tail)
                val tailPath = Path().apply {
                    moveTo(center.x, center.y + needleLength * 0.5f)
                    lineTo(center.x - 8.dp.toPx(), center.y + 15.dp.toPx())
                    lineTo(center.x, center.y)
                    lineTo(center.x + 8.dp.toPx(), center.y + 15.dp.toPx())
                    close()
                }
                drawPath(
                    path = tailPath,
                    color = Color(0x5594A3B8)
                )

                // Center Pin Circle
                drawCircle(
                    color = Color(0xFFF59E0B),
                    radius = 12.dp.toPx(),
                    center = center
                )
                drawCircle(
                    color = Color.White,
                    radius = 5.dp.toPx(),
                    center = center
                )
            }

            // Kaaba Icon at the tip of the dial
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(animatedRelativeAngle)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF1E293B),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp)
                        .size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "🕋",
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Bottom Info & Calibration Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 96.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Distance Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Sudut Kiblat",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = String.format(Locale.getDefault(), "%.1f°", qiblaBearing),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .height(30.dp)
                            .width(1.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Jarak ke Ka'bah",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${String.format(Locale.getDefault(), "%,.0f", uiState.distanceToKaabaKm)} km",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFF59E0B)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .height(30.dp)
                            .width(1.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Arah Kompas",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = String.format(Locale.getDefault(), "%.0f°", currentAzimuth),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Calibration & Tips
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Letakkan ponsel di permukaan datar & jauhkan dari benda logam/magnet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
    }
}
