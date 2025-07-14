package com.ganadoro.pile.repositories

import android.content.Context
import android.graphics.Bitmap
import com.ganadoro.pile.util.renderFirstPdfPage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File

class BitmapCacheRepository (
    private val appContext: Context
) {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _bitmapCache = MutableStateFlow<Map<String, Bitmap>>(emptyMap())
    val bitmapCache = _bitmapCache.asStateFlow()

    private val loadingMutex = Mutex()

    /**
     * Inicia la carga de un bitmap para un documentId si no está ya en la caché.
     * Esta función devuelve el control inmediatamente (no es suspend).
     */
    fun ensureBitmapIsLoaded(documentId: String) {
        if (_bitmapCache.value.containsKey(documentId)) {
            return
        }

        repositoryScope.launch {
            loadingMutex.withLock {
                if (_bitmapCache.value.containsKey(documentId)) {
                    return@launch
                }

                val file = File(appContext.filesDir, documentId)

                val bitmap = renderFirstPdfPage(file)

                if (bitmap != null) {
                    _bitmapCache.value += (documentId to bitmap)
                }
            }
        }
    }
}