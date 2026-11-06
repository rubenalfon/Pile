package es.pile.core.domain.models.ocr

import java.io.File

/**
 * Encapsulates the local file references required to initialize the ONNX OCR engine.
 *
 * @property detectorFile The `.onnx` file containing the text detection model (e.g. finds text bounding boxes).
 * @property recognizerFile The `.onnx` file containing the text recognition model (e.g. reads text from bounding boxes).
 * @property dictionaryFile The `.txt` file containing the character mapping dictionary used to decode recognizer output.
 */
data class OcrModelFiles(
    val detectorFile: File,
    val recognizerFile: File,
    val dictionaryFile: File
)
