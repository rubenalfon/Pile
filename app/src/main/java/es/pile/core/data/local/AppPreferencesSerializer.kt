package es.pile.core.data.local

import androidx.datastore.core.Serializer
import es.pile.core.domain.models.AppPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

/**
 * Serializer for [AppPreferences] used by DataStore to persist app preferences.
 */
object AppPreferencesSerializer : Serializer<AppPreferences> {
    override val defaultValue: AppPreferences = AppPreferences()

    /**
     * Reads [AppPreferences] from the provided [InputStream].
     *
     * @param input The input stream to read from.
     * @return The [AppPreferences] read from the input stream.
     */
    override suspend fun readFrom(input: InputStream): AppPreferences {
        return try {
            Json.decodeFromString(
                deserializer = AppPreferences.serializer(),
                string = input.readBytes().decodeToString()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            defaultValue
        }
    }

    /**
     * Writes [AppPreferences] to the provided [OutputStream].
     *
     * @param t The [AppPreferences] to write.
     * @param output The output stream to write to.
     */
    override suspend fun writeTo(t: AppPreferences, output: OutputStream) {
        withContext(Dispatchers.IO) {
            output.write(
                Json.encodeToString(
                    serializer = AppPreferences.serializer(), value = t
                ).toByteArray()
            )
        }
    }
}