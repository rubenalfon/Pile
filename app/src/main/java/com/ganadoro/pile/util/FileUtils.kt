package com.ganadoro.pile.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns


object FileUtils {
    /**
     * Obtiene el nombre de archivo visible para el usuario a partir de una Uri de contenido.
     *
     * @param context El contexto de la aplicación.
     * @param uri La Uri del archivo.
     * @return El nombre del archivo como un String, o null si no se puede resolver.
     */
    fun getFileNameFromUri(context: Context, uri: Uri): String? {
        var fileName: String? = null
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex)
                }
            }
        }
        fileName = fileName?.substringBeforeLast('.')
        return fileName
    }
}


