package es.pile.features.editDocument.data.helper

import android.graphics.Bitmap
import com.tanishranjan.cropkit.CropColors
import com.tanishranjan.cropkit.CropController
import com.tanishranjan.cropkit.CropOptions
import es.pile.features.editDocument.domain.helper.CropControllerFactory

class CropControllerFactoryImpl : CropControllerFactory {
    override fun create(
        bitmap: Bitmap,
        cropColors: CropColors,
        cropOptions: CropOptions
    ): CropController {
        return CropController(
            bitmap = bitmap,
            cropColors = cropColors,
            cropOptions = cropOptions
        )
    }
}
