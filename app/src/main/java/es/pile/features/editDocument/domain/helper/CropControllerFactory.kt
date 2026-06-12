package es.pile.features.editDocument.domain.helper

import android.graphics.Bitmap
import com.tanishranjan.cropkit.CropColors
import com.tanishranjan.cropkit.CropController
import com.tanishranjan.cropkit.CropOptions

/**
 * Factory interface to abstract the creation of [CropController] instances.
 * This assists in decoupling domain logic from third-party UI components
 * and facilitates unit testing.
 */
interface CropControllerFactory {
    fun create(
        bitmap: Bitmap,
        cropColors: CropColors,
        cropOptions: CropOptions
    ): CropController
}
