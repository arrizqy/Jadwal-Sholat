package com.example.location

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import com.example.data.model.UserLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

data class CityPreset(
    val name: String,
    val province: String,
    val latitude: Double,
    val longitude: Double
)

class LocationHelper(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("prayer_location_prefs", Context.MODE_PRIVATE)
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    companion object {
        val INDONESIAN_CITIES = listOf(
            CityPreset("Jakarta Pusat", "DKI Jakarta", -6.1805, 106.8284),
            CityPreset("Jakarta Selatan", "DKI Jakarta", -6.2615, 106.8106),
            CityPreset("Surabaya", "Jawa Timur", -7.2575, 112.7521),
            CityPreset("Bandung", "Jawa Barat", -6.9175, 107.6191),
            CityPreset("Medan", "Sumatera Utara", 3.5952, 98.6722),
            CityPreset("Semarang", "Jawa Tengah", -6.9667, 110.4167),
            CityPreset("Makassar", "Sulawesi Selatan", -5.1477, 119.4327),
            CityPreset("Palembang", "Sumatera Selatan", -2.9761, 104.7754),
            CityPreset("Yogyakarta", "D.I. Yogyakarta", -7.7956, 110.3695),
            CityPreset("Denpasar", "Bali", -8.6705, 115.2126),
            CityPreset("Banda Aceh", "Aceh", 5.5483, 95.3238),
            CityPreset("Padang", "Sumatera Barat", -0.9471, 100.4172),
            CityPreset("Pekanbaru", "Riau", 0.5071, 101.4478),
            CityPreset("Banjarmasin", "Kalimantan Selatan", -3.3194, 114.5908),
            CityPreset("Samarinda", "Kalimantan Timur", -0.5022, 117.1537),
            CityPreset("Pontianak", "Kalimantan Barat", -0.0263, 109.3425),
            CityPreset("Manado", "Sulawesi Utara", 1.4748, 124.8428),
            CityPreset("Mataram", "Nusa Tenggara Barat", -8.5833, 116.1167),
            CityPreset("Kupang", "Nusa Tenggara Timur", -10.1772, 123.6070),
            CityPreset("Ambon", "Maluku", -3.6547, 128.1906),
            CityPreset("Jayapura", "Papua", -2.5337, 140.7181),
            CityPreset("Makkah", "Arab Saudi", 21.4225, 39.8262),
            CityPreset("Madinah", "Arab Saudi", 24.4672, 39.6111)
        )
    }

    fun getSavedLocation(): UserLocation {
        val lat = prefs.getFloat("pref_lat", -6.2088f).toDouble()
        val lng = prefs.getFloat("pref_lng", 106.8456f).toDouble()
        val city = prefs.getString("pref_city", "Jakarta Pusat") ?: "Jakarta Pusat"
        val region = prefs.getString("pref_region", "DKI Jakarta, Indonesia") ?: "DKI Jakarta, Indonesia"
        val auto = prefs.getBoolean("pref_auto", true)
        return UserLocation(lat, lng, city, region, auto)
    }

    fun saveLocation(location: UserLocation) {
        prefs.edit()
            .putFloat("pref_lat", location.latitude.toFloat())
            .putFloat("pref_lng", location.longitude.toFloat())
            .putString("pref_city", location.cityName)
            .putString("pref_region", location.regionName)
            .putBoolean("pref_auto", location.isAutoDetected)
            .apply()
    }

    @SuppressLint("MissingPermission")
    suspend fun fetchCurrentLocation(): UserLocation? = withContext(Dispatchers.IO) {
        try {
            // Try FusedLocationClient first
            val location: Location? = suspendCancellableCoroutine { continuation ->
                try {
                    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                        .addOnSuccessListener { loc ->
                            continuation.resume(loc)
                        }
                        .addOnFailureListener {
                            continuation.resume(null)
                        }
                } catch (e: Exception) {
                    continuation.resume(null)
                }
            } ?: run {
                // Fallback to Android LocationManager
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                val gpsLoc = try { locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER) } catch (_: Exception) { null }
                val netLoc = try { locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) } catch (_: Exception) { null }
                gpsLoc ?: netLoc
            }

            if (location != null) {
                val geocoded = reverseGeocode(location.latitude, location.longitude)
                val userLoc = UserLocation(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    cityName = geocoded.first,
                    regionName = geocoded.second,
                    isAutoDetected = true
                )
                saveLocation(userLoc)
                userLoc
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun reverseGeocode(lat: Double, lng: Double): Pair<String, String> {
        return try {
            val geocoder = Geocoder(context, Locale("id", "ID"))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Synchronous fallback since we are already in IO dispatcher
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                formatAddress(addresses?.firstOrNull(), lat, lng)
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                formatAddress(addresses?.firstOrNull(), lat, lng)
            }
        } catch (e: Exception) {
            // Find closest preset city if reverse geocoding fails
            findClosestCity(lat, lng)
        }
    }

    private fun formatAddress(address: Address?, lat: Double, lng: Double): Pair<String, String> {
        if (address == null) return findClosestCity(lat, lng)
        val city = address.subAdminArea ?: address.locality ?: address.subLocality ?: "Kota Terdeteksi"
        val state = address.adminArea ?: address.countryName ?: "Indonesia"
        return Pair(city, "$state, ${address.countryName ?: "Indonesia"}")
    }

    private fun findClosestCity(lat: Double, lng: Double): Pair<String, String> {
        var minDistance = Double.MAX_VALUE
        var closest = INDONESIAN_CITIES.first()
        for (c in INDONESIAN_CITIES) {
            val d = Math.hypot(lat - c.latitude, lng - c.longitude)
            if (d < minDistance) {
                minDistance = d
                closest = c
            }
        }
        return Pair(closest.name, "${closest.province}, Indonesia")
    }
}
