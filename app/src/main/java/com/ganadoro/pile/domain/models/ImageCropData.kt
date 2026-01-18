package com.ganadoro.pile.domain.models

/**
 * Represents the cropping parameters for an image.
 * @property x The x-coordinate of the top-left corner of the crop rectangle.
 * @property y The y-coordinate of the top-left corner of the crop rectangle.
 * @property width The width of the crop rectangle.
 * @property height The height of the crop rectangle.
 */
data class ImageCropData(
    val x:      Int,
    val y:      Int,
    val width:  Int,
    val height: Int
)