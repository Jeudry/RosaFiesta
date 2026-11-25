package com.jeudry.rosafiesta.data

import com.jeudry.rosafiesta.domain.MapPlaceSuggestion
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.readRawBytes
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class PhotonResponseDto(
    val features: List<PhotonFeatureDto> = emptyList(),
)

@Serializable
private data class PhotonFeatureDto(
    val properties: PhotonPropertiesDto = PhotonPropertiesDto(),
    val geometry: PhotonGeometryDto = PhotonGeometryDto(),
)

@Serializable
private data class PhotonPropertiesDto(
    val name: String? = null,
    val street: String? = null,
    val locality: String? = null,
    val city: String? = null,
    val state: String? = null,
    val country: String? = null,
)

@Serializable
private data class PhotonGeometryDto(
    val coordinates: List<Double> = emptyList(),
)

/**
 * Fetches live OpenStreetMap raster tiles and Photon geocoding results across all platforms.
 */
public class InteractiveMapClient(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val httpClient: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = REQUEST_TIMEOUT_MS
            connectTimeoutMillis = CONNECT_TIMEOUT_MS
            socketTimeoutMillis = SOCKET_TIMEOUT_MS
        }
    },
) {
    /**
     * Releases underlying HTTP client resources when the map picker leaves composition.
     */
    public fun close() {
        httpClient.close()
    }

    /**
     * Searches real places matching [query], biased gently near the optional [biasLat] and [biasLon].
     */
    public suspend fun searchPlaces(
        query: String,
        biasLat: Double = DEFAULT_SANTO_DOMINGO_LAT,
        biasLon: Double = DEFAULT_SANTO_DOMINGO_LON,
    ): List<MapPlaceSuggestion> = withContext(ioDispatcher) {
        val cleanQuery = query.trim()
        if (cleanQuery.isEmpty()) {
            return@withContext emptyList()
        }
        val response = runCatching {
            httpClient.get("https://photon.komoot.io/api/") {
                parameter("q", cleanQuery)
                parameter("limit", "6")
                parameter("lat", biasLat.toString())
                parameter("lon", biasLon.toString())
            }
        }.getOrNull() ?: return@withContext emptyList()
        if (!response.status.isSuccess()) {
            return@withContext emptyList()
        }
        val payload = runCatching { response.body<PhotonResponseDto>() }.getOrNull()
            ?: return@withContext emptyList()
        payload.features.mapNotNull { feature -> feature.toSuggestionOrNull() }
    }

    /**
     * Resolves a human-readable street or venue label for the given [latitude] and [longitude].
     */
    public suspend fun reverseGeocode(
        latitude: Double,
        longitude: Double,
    ): String? = withContext(ioDispatcher) {
        val response = runCatching {
            httpClient.get("https://photon.komoot.io/reverse") {
                parameter("lat", latitude.toString())
                parameter("lon", longitude.toString())
            }
        }.getOrNull() ?: return@withContext null
        if (!response.status.isSuccess()) {
            return@withContext null
        }
        val payload = runCatching { response.body<PhotonResponseDto>() }.getOrNull()
            ?: return@withContext null
        payload.features.firstOrNull()?.toSuggestionOrNull()?.shortTitle
    }

    /**
     * Downloads a 256x256 PNG slippy map tile from CartoDB Voyager for ([zoom], [tileX], [tileY]).
     */
    public suspend fun fetchMapTilePng(
        zoom: Int,
        tileX: Int,
        tileY: Int,
    ): ByteArray? = withContext(ioDispatcher) {
        val subdomain = TILE_SUBDOMAINS[(tileX + tileY).mod(TILE_SUBDOMAINS.size)]
        val tileUrl = "https://$subdomain.basemaps.cartocdn.com/rastertiles/voyager/$zoom/$tileX/$tileY.png"
        val response = runCatching { httpClient.get(tileUrl) }.getOrNull() ?: return@withContext null
        if (!response.status.isSuccess()) {
            return@withContext null
        }
        runCatching { response.readRawBytes() }.getOrNull()
    }

    private fun PhotonFeatureDto.toSuggestionOrNull(): MapPlaceSuggestion? {
        val lon = geometry.coordinates.getOrNull(0) ?: return null
        val lat = geometry.coordinates.getOrNull(1) ?: return null
        val rawParts = listOf(
            properties.name,
            properties.street,
            properties.locality,
            properties.city,
            properties.state,
            properties.country,
        )
        val uniqueParts = mutableListOf<String>()
        for (rawPart in rawParts) {
            val cleaned = rawPart?.trim().orEmpty()
            val isNewPart = cleaned.isNotEmpty() && cleaned !in uniqueParts
            if (isNewPart) {
                uniqueParts.add(cleaned)
            }
        }
        if (uniqueParts.isEmpty()) {
            return null
        }
        val shortTitle = uniqueParts.take(2).joinToString(", ")
        val fullDisplay = uniqueParts.joinToString(", ")
        return MapPlaceSuggestion(
            displayName = fullDisplay,
            shortTitle = shortTitle,
            latitude = lat,
            longitude = lon,
        )
    }

    public companion object {
        public const val DEFAULT_SANTO_DOMINGO_LAT: Double = 18.4861
        public const val DEFAULT_SANTO_DOMINGO_LON: Double = -69.9312
        private const val REQUEST_TIMEOUT_MS: Long = 20_000L
        private const val CONNECT_TIMEOUT_MS: Long = 10_000L
        private const val SOCKET_TIMEOUT_MS: Long = 20_000L
        private val TILE_SUBDOMAINS = listOf("a", "b", "c", "d")
    }
}
