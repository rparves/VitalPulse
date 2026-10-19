package com.vitapulse.app.food

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.URL
import java.net.URLEncoder
import javax.net.ssl.HttpsURLConnection

/**
 * Open Food Facts — free, open (ODbL) product database. No API key.
 * Only the barcode or search text is sent; no personal data leaves the device.
 */
object OpenFoodFacts {
    private const val UA = "VitaPulse/1.0 (Android; open-source fitness app)"
    private const val FIELDS = "product_name,brands,nutriments,serving_quantity"

    suspend fun byBarcode(code: String): FoodCandidate? = withContext(Dispatchers.IO) {
        if (code.length !in 6..14 || !code.all { it.isDigit() }) return@withContext null
        val body = get("https://world.openfoodfacts.org/api/v2/product/$code.json?fields=$FIELDS")
            ?: return@withContext null
        runCatching {
            val obj = JSONObject(body)
            if (obj.optInt("status") != 1) null else parse(obj.getJSONObject("product"))
        }.getOrNull()
    }

    suspend fun search(query: String): List<FoodCandidate> = withContext(Dispatchers.IO) {
        val q = query.trim().take(60)
        if (q.length < 2) return@withContext emptyList()
        val enc = URLEncoder.encode(q, "UTF-8")
        val body = get(
            "https://world.openfoodfacts.org/cgi/search.pl?search_terms=$enc" +
                "&search_simple=1&action=process&json=1&page_size=12&fields=$FIELDS"
        ) ?: return@withContext emptyList()
        runCatching {
            val arr = JSONObject(body).optJSONArray("products") ?: return@runCatching emptyList()
            (0 until arr.length()).mapNotNull { parse(arr.getJSONObject(it)) }
        }.getOrDefault(emptyList())
    }

    private fun parse(p: JSONObject): FoodCandidate? {
        val n = p.optJSONObject("nutriments") ?: return null
        var kcal = n.optDouble("energy-kcal_100g", Double.NaN)
        if (kcal.isNaN()) kcal = n.optDouble("energy_100g", Double.NaN) / 4.184
        if (kcal.isNaN() || kcal < 0 || kcal > 950) return null
        val name = p.optString("product_name").trim()
        if (name.isBlank()) return null
        val brand = p.optString("brands").split(",").firstOrNull()?.trim().orEmpty()
        val serving = p.optDouble("serving_quantity", Double.NaN)
        return FoodCandidate(
            name = (if (brand.isNotBlank()) "$name · $brand" else name).take(120),
            kcal100 = kcal,
            protein100 = n.optDouble("proteins_100g", 0.0).takeIf { !it.isNaN() } ?: 0.0,
            carbs100 = n.optDouble("carbohydrates_100g", 0.0).takeIf { !it.isNaN() } ?: 0.0,
            fat100 = n.optDouble("fat_100g", 0.0).takeIf { !it.isNaN() } ?: 0.0,
            servingG = if (!serving.isNaN() && serving in 1.0..2000.0) serving else 100.0,
            source = "Open Food Facts",
        )
    }

    private fun get(url: String): String? {
        val c = URL(url).openConnection() as HttpsURLConnection
        return try {
            c.connectTimeout = 8000
            c.readTimeout = 12000
            c.setRequestProperty("User-Agent", UA)
            c.setRequestProperty("Accept", "application/json")
            if (c.responseCode != 200) null
            else c.inputStream.bufferedReader().use { r ->
                // Cap response size to avoid memory abuse.
                val sb = StringBuilder()
                val buf = CharArray(8192)
                while (true) {
                    val n = r.read(buf)
                    if (n < 0) break
                    sb.append(buf, 0, n)
                    if (sb.length > 3_000_000) return null
                }
                sb.toString()
            }
        } catch (e: IOException) {
            null
        } finally {
            c.disconnect()
        }
    }
}
