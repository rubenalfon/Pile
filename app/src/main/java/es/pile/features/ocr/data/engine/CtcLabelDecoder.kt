package es.pile.features.ocr.data.engine

import java.io.File

/**
 * A decoder that implements Connectionist Temporal Classification (CTC) to translate
 * raw neural network outputs into human-readable strings.
 *
 * CTC is used in sequence-to-sequence tasks, like Optical Character Recognition (OCR),
 * where the alignment between the input features (image slices) and the output labels (text)
 * is not known beforehand.
 *
 * It operates by:
 * 1. Identifying the most probable character class at each time step (including a "blank" token).
 * 2. Merging consecutive identical characters into a single character.
 * 3. Removing all "blank" tokens.
 *
 * For example, a raw sequence of predictions like "hh-e-ll-ll--oo" (where '-' is the blank token)
 * will be correctly decoded into the string "hello".
 *
 * @param dictFile The dictionary file containing the list of valid characters the model can predict.
 */
class CtcLabelDecoder(dictFile: File) {
    private val characterList = mutableListOf<String>()

    init {
        // The 0-th index in PaddleOCR is the CTC blank token
        characterList.add("blank")

        // Read lines from the dictionary file
        dictFile.readLines().forEach { line ->
            characterList.add(line)
        }

        // In many models, the last token is an added space ' ' token.
        // We'll append it just in case to match common paddle dictionaries size
        characterList.add(" ")
    }

    /**
     * Decodes the raw neural network probability tensor into a text string.
     *
     * @param predictions A flat array representing a tensor of shape `[1, timeSteps, numClasses]`.
     * @param timeSteps The number of slices/frames the image was divided into.
     * @param numClasses The number of possible characters (including the blank token).
     * @return A pair containing the final decoded string and its average confidence score (from 0.0 to 1.0).
     */
    fun decode(predictions: FloatArray, timeSteps: Int, numClasses: Int): Pair<String, Float> {
        val sb = StringBuilder()
        var lastIndex = -1
        var totalScore = 0f
        var validCount = 0

        for (t in 0 until timeSteps) {
            val offset = t * numClasses
            var maxScore = -Float.MAX_VALUE
            var maxIndex = 0

            for (c in 0 until numClasses) {
                val score = predictions[offset + c]
                if (score > maxScore) {
                    maxScore = score
                    maxIndex = c
                }
            }

            // In CTC, we ignore the blank token (index 0) and collapse consecutive identical characters
            if (maxIndex != 0 && maxIndex != lastIndex) {
                if (maxIndex < characterList.size) {
                    val charStr = characterList[maxIndex]
                    sb.append(charStr)
                    totalScore += maxScore
                    validCount++
                }
            }

            lastIndex = maxIndex
        }

        val avgConfidence = if (validCount > 0) totalScore / validCount else 0f
        return Pair(sb.toString(), avgConfidence)
    }
}
