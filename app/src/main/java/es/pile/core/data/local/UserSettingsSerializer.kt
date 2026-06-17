package es.pile.core.data.local

import androidx.datastore.core.Serializer
import es.pile.core.domain.models.UserSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

/**
 * Serializer for [UserSettings] used by DataStore to persist user preferences.
 */
object UserSettingsSerializer : Serializer<UserSettings> {
    /**
     * The default [UserSettings] value when no data is available.
     */
    override val defaultValue: UserSettings = UserSettings()

    /**
     * Reads [UserSettings] from the provided [InputStream].
     *
     * @param input The input stream to read from.
     * @return The [UserSettings] read from the input stream.
     */
    override suspend fun readFrom(input: InputStream): UserSettings {
        return try {
            Json.decodeFromString(
                deserializer = UserSettings.serializer(),
                string = input.readBytes().decodeToString()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            defaultValue
        }
    }

    /**
     * Writes [UserSettings] to the provided [OutputStream].
     *
     * @param t The [UserSettings] to write.
     * @param output The output stream to write to.
     */
    override suspend fun writeTo(t: UserSettings, output: OutputStream) {
        withContext(Dispatchers.IO) {
            output.write(
                Json.encodeToString(
                    serializer = UserSettings.serializer(), value = t
                ).toByteArray()
            )
        }
    }
}