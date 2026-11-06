package es.pile.core.domain.models.ocr

/**
 * Represents the complete output of an Optical Character Recognition (OCR) operation
 * performed on a single image or page.
 *
 * @property fullText The complete recognized text, usually constructed by concatenating all
 * [blocks] with appropriate spacing or newlines.
 * @property blocks A list of individual [OcrBlock]s detected in the image.
 */
data class OcrResult(
    val fullText: String,
    val blocks: List<OcrBlock>
)

/**
 * Represents a single detected block of text within an image.
 *
 * @property text The recognized text content of this block.
 * @property confidence A value between 0.0 and 1.0 indicating the certainty of the OCR prediction.
 * @property boundingBox The geometric boundaries of the text block, defined by exactly 4 [Point]s.
 * Typically ordered as: top-left, top-right, bottom-right, bottom-left.
 */
data class OcrBlock(
    val text: String,
    val confidence: Float,
    val boundingBox: List<Point>
)

/**
 * Represents a 2D coordinate on an image.
 *
 * @property x The horizontal coordinate.
 * @property y The vertical coordinate.
 */
data class Point(val x: Float, val y: Float)
