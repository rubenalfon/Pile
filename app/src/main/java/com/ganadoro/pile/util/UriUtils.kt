package com.ganadoro.pile.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object UriUtils {
    /**
     * Crea una Uri para un nuevo archivo de imagen en el directorio de caché de la app.
     * Esta Uri es segura para ser compartida con otras apps (como la cámara) a través de FileProvider.
     *
     * @param context El contexto de la aplicación.
     * @return La Uri del nuevo archivo de imagen creado.
     */
    fun createImageUri(context: Context): Uri {
        val imageDir = File(context.cacheDir, "images")
        if (!imageDir.exists()) {
            imageDir.mkdirs()
        }

        val imageFile = File.createTempFile(
            "JPEG_${System.currentTimeMillis()}_",
            ".jpg",
            imageDir
        )

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            imageFile
        )
    }
}