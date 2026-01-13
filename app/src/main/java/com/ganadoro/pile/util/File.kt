package com.ganadoro.pile.util

import android.content.Context
import android.net.Uri
import java.io.File

/**
 * Copia el contenido de una Uri a un nuevo archivo en el almacenamiento interno de la app.
 *
 * @param context El contexto de la aplicación.
 * @param uri La Uri del archivo a copiar (obtenida del selector de archivos).
 * @return El objeto File del nuevo archivo creado en el almacenamiento interno.
 * @throws Exception si ocurre un error durante la copia.
 */
fun File.copyUriFile(context: Context, uri: Uri): File {
    val resolver = context.contentResolver

    val inputStream = resolver.openInputStream(uri)
        ?: throw IllegalStateException("No se pudo abrir el stream para la URI: $uri")

    inputStream.use { input ->
        this.outputStream().use { output ->
            input.copyTo(output)
        }
    }

    return this
}