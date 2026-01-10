package com.ganadoro.pile.repositories

import android.content.Context
import android.graphics.Bitmap
import com.ganadoro.pile.util.prepareBitmapFromFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

/**
 * Repositorio que gestiona la caché de bitmaps.
 * @param appContext Contexto de la aplicación.
 */
class BitmapCacheRepository (
    private val appContext: Context
) {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _bitmapCache = MutableStateFlow<Map<String, Bitmap>>(emptyMap())
    val bitmapCache = _bitmapCache.asStateFlow()

    /**
     * Inicia la carga de un bitmap.
     * @param imageId Se usa como llave única en la caché.
     */
    fun ensureBitmapIsLoaded(documentId: String, imageId: String) {
        if (_bitmapCache.value.containsKey(imageId)) return

        repositoryScope.launch {
            if (_bitmapCache.value.containsKey(imageId)) return@launch

            val folderName = documentId
            val documentFolder = File(appContext.filesDir, folderName)
            val imageFile = File(documentFolder, imageId)

            if (!imageFile.exists()) return@launch

            val bitmap = prepareBitmapFromFile(imageFile)

            if (bitmap != null) {
                _bitmapCache.update { currentCache ->
                    currentCache + (imageId to bitmap)
                }
            }
        }
    }

    /**
     * Borra un bitmap de la cache.
     * @param imageId id del bitmap a borrar.
     */
    fun removeFromCache(imageId: String) {
        _bitmapCache.update { current ->
            val bitmap = current[imageId]
            if (bitmap != null && !bitmap.isRecycled) {
                bitmap.recycle()
            }
            current - imageId
        }
    }

    /**
     * Libera todos los bitmaps de la memoria RAM.
     */
    fun clearCache() {
        val bitmapsToRecycle = _bitmapCache.value.values

        _bitmapCache.update { emptyMap() }

        bitmapsToRecycle.forEach { bitmap ->
            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }
        }
    }
}