package es.pile.features.ocr.data.engine

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.system.Os
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import es.pile.core.domain.models.ocr.OcrBlock
import es.pile.core.domain.models.ocr.OcrResult
import es.pile.core.domain.models.ocr.Point
import es.pile.core.domain.repositories.OcrModelRepository
import es.pile.features.ocr.data.engine.OnnxOcrEngine.Companion.DET_MAX_SIDE_LEN
import es.pile.features.ocr.data.engine.OnnxOcrEngine.Companion.DET_STRIDE
import es.pile.features.ocr.data.engine.OnnxOcrEngine.Companion.DET_UNCLIP_RATIO
import es.pile.features.ocr.data.engine.OnnxOcrEngine.Companion.REC_IMAGE_HEIGHT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Collections
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Core engine responsible for performing offline Optical Character Recognition (OCR) on images
 * using ONNX Runtime and PaddleOCR v3 models.
 *
 * This engine utilizes a two-step deep learning pipeline:
 * 1. **Detection**: Analyzes the full image once using a language-agnostic model (`det.onnx`) to find
 *    bounding boxes around text regions.
 * 2. **Recognition**: Crops each detected text region, normalizes it, and evaluates it against one or
 *    more language-specific recognition models (`rec.onnx` + `dict.txt`), keeping the highest-confidence
 *    transcription for each region.
 *
 * @property ocrModelRepository Repository used to resolve and fetch local file paths of the required ONNX models.
 */
class OnnxOcrEngine(
    private val ocrModelRepository: OcrModelRepository
) : AutoCloseable {

    /**
     * Process-wide ONNX Runtime host environment configured with telemetry explicitly disabled
     * at both OS process level and API level before loading native libraries.
     */
    private val env: OrtEnvironment by lazy {
        try {
            Os.setenv("ORT_DISABLE_TELEMETRY", "1", true)
        } catch (_: Exception) {
            // Ignore OS restrictions on environment variables
        }
        OrtEnvironment.getEnvironment().apply {
            setTelemetry(false)
        }
    }

    /**
     * Mutex ensuring that model initialization and inference never overlap across coroutines.
     */
    private val engineMutex = Mutex()

    /**
     * Lock guarding lifecycle state transitions between active inferences and [close].
     */
    private val closeLock = Any()

    /**
     * Tracks how many model loading or inference operations are currently executing.
     */
    private var activeOperations = 0

    /**
     * Indicates whether [close] has been requested while operations were in flight.
     */
    private var isCloseRequested = false

    /**
     * Shared ONNX session for the language-agnostic text detector model (`det.onnx`).
     */
    private var detSession: OrtSession? = null

    /**
     * Cache of active language recognition pipelines keyed by their `modelGroupId` (e.g., "latin", "chinese").
     */
    private val activeRecognizers = mutableMapOf<String, RecognizerPipeline>()

    /**
     * Reusable bilinear filtering paint for smooth bitmap scaling and rotation on Canvas.
     */
    private val bitmapPaint = Paint(Paint.FILTER_BITMAP_FLAG)

    /**
     * Bundles a language-specific ONNX recognition session with its corresponding character dictionary decoder.
     */
    private class RecognizerPipeline(
        val session: OrtSession,
        val decoder: CtcLabelDecoder
    ) : AutoCloseable {
        override fun close() {
            session.close()
        }
    }

    /**
     * Internal representation of a detected text region in heatmap coordinates.
     *
     * @property score Average probability score from the detection heatmap for this region.
     */
    private class Box(
        val minX: Int,
        val minY: Int,
        val maxX: Int,
        val maxY: Int,
        val score: Float
    )

    /**
     * Initializes and caches the shared detector model and all requested language recognition models.
     * Models that are already loaded in memory are reused without reloading from disk.
     *
     * @param modelGroupIds Set of language group identifiers to keep loaded in memory.
     * @throws IllegalStateException If any requested model group is not downloaded locally.
     */
    suspend fun initModels(modelGroupIds: Set<String>) = withContext(Dispatchers.IO) {
        require(modelGroupIds.isNotEmpty()) { "At least one modelGroupId must be provided." }

        engineMutex.withLock {
            beginOperation()
            try {
                loadModelsInternal(modelGroupIds)
            } finally {
                endOperation()
            }
        }
    }

    /**
     * Performs OCR on the given [bitmap] using a single language [modelGroupId].
     */
    suspend fun runOcr(bitmap: Bitmap, modelGroupId: String): OcrResult =
        runOcr(bitmap, setOf(modelGroupId))

    /**
     * Performs end-to-end OCR on the given [bitmap] across all specified [modelGroupIds].
     *
     * Text detection and image cropping are executed only once per image. If multiple language models
     * are provided, each cropped text box is evaluated against all active recognizers and the
     * transcription with the highest confidence score is selected.
     *
     * @param bitmap Source document image to analyze.
     * @param modelGroupIds Active language model group IDs selected by the user.
     * @return Structured [OcrResult] containing the full reconstructed text and individual [OcrBlock]s.
     */
    suspend fun runOcr(bitmap: Bitmap, modelGroupIds: Set<String>): OcrResult =
        withContext(Dispatchers.Default) {
            if (modelGroupIds.isEmpty()) {
                return@withContext OcrResult(fullText = "", blocks = emptyList())
            }

            engineMutex.withLock {
                beginOperation()
                try {
                    withContext(Dispatchers.IO) {
                        loadModelsInternal(modelGroupIds)
                    }
                    currentCoroutineContext().ensureActive()

                    val detector = detSession ?: throw IllegalStateException("Detector session not initialized.")
                    val recognizers = modelGroupIds.mapNotNull { activeRecognizers[it] }
                    if (recognizers.isEmpty()) {
                        throw IllegalStateException("No recognizer sessions initialized.")
                    }

                    val (workingBitmap, isTempSoftwareCopy) = ensureSoftwareBitmap(bitmap)
                    try {
                        // 1. Preprocess image and run text detection
                        val (detWidth, detHeight) = calculateDetectionSize(workingBitmap.width, workingBitmap.height)
                        val resizedDetBitmap = workingBitmap.scale(detWidth, detHeight)

                        val rawBoxes = try {
                            bitmapToNormalizedTensor(
                                bitmap = resizedDetBitmap,
                                mean = IMAGENET_MEAN,
                                std = IMAGENET_STD
                            ).use { detInputTensor ->
                                val detInputName = detector.inputNames.first()
                                detector.run(Collections.singletonMap(detInputName, detInputTensor)).use { detResults ->
                                    @Suppress("UNCHECKED_CAST")
                                    val detOutput = detResults[0].value as Array<Array<Array<FloatArray>>>
                                    val heatMap = detOutput[0][0] // Shape: [H, W]
                                    findTextBoxes(heatMap)
                                }
                            }
                        } finally {
                            // Recycle temporary scaled bitmap without touching the caller's bitmap
                            if (resizedDetBitmap !== workingBitmap) {
                                resizedDetBitmap.recycle()
                            }
                        }

                        currentCoroutineContext().ensureActive()

                        // 2. Sort detected boxes in natural reading order (top-to-bottom rows, left-to-right per row)
                        val sortedBoxes = sortBoxesInReadingOrder(rawBoxes)

                        // Scale factors to map heatmap coordinates back to the original bitmap dimensions
                        val scaleX = workingBitmap.width.toFloat() / detWidth
                        val scaleY = workingBitmap.height.toFloat() / detHeight

                        val ocrBlocks = mutableListOf<OcrBlock>()
                        val fullTextBuilder = StringBuilder()
                        var lastY = -1f
                        var lastHeight = -1f

                        // 3. Crop each box once and evaluate across active recognizers
                        for (box in sortedBoxes) {
                            currentCoroutineContext().ensureActive()

                            val minX = max(0, (box.minX * scaleX).toInt())
                            val minY = max(0, (box.minY * scaleY).toInt())
                            val maxX = min(workingBitmap.width, (box.maxX * scaleX).toInt())
                            val maxY = min(workingBitmap.height, (box.maxY * scaleY).toInt())

                            if (maxX <= minX || maxY <= minY) continue

                            val recBitmap = prepareCropForRecognition(
                                source = workingBitmap,
                                minX = minX,
                                minY = minY,
                                width = maxX - minX,
                                height = maxY - minY
                            )

                            val (bestText, bestConf) = try {
                                bitmapToNormalizedTensor(
                                    bitmap = recBitmap,
                                    mean = REC_MEAN,
                                    std = REC_STD
                                ).use { recInputTensor ->
                                    recognizeBoxWithBestModel(recInputTensor, recognizers)
                                }
                            } finally {
                                recBitmap.recycle()
                            }

                            if (bestConf > MIN_RECOGNITION_CONFIDENCE && bestText.isNotBlank()) {
                                ocrBlocks.add(
                                    OcrBlock(
                                        text = bestText,
                                        confidence = bestConf,
                                        boundingBox = listOf(
                                            Point(minX.toFloat(), minY.toFloat()),
                                            Point(maxX.toFloat(), minY.toFloat()),
                                            Point(maxX.toFloat(), maxY.toFloat()),
                                            Point(minX.toFloat(), maxY.toFloat())
                                        )
                                    )
                                )

                                // Insert a line break if the vertical jump exceeds half the average row height
                                val boxHeight = (maxY - minY).toFloat()
                                if (lastY != -1f) {
                                    val avgHeight = (lastHeight + boxHeight) / 2f
                                    val isNewRow = (minY - lastY) > (avgHeight * NEW_LINE_HEIGHT_RATIO)
                                    fullTextBuilder.append(if (isNewRow) "\n" else " ")
                                }
                                fullTextBuilder.append(bestText)
                                lastY = minY.toFloat()
                                lastHeight = boxHeight
                            }
                        }

                        OcrResult(
                            fullText = fullTextBuilder.toString(),
                            blocks = ocrBlocks
                        )
                    } finally {
                        if (isTempSoftwareCopy) {
                            workingBitmap.recycle()
                        }
                    }
                } finally {
                    endOperation()
                }
            }
        }

    /**
     * Marks the start of an active model load or inference operation so [close] does not
     * free native sessions mid-execution.
     */
    private fun beginOperation() {
        synchronized(closeLock) {
            isCloseRequested = false
            activeOperations++
        }
    }

    /**
     * Marks the completion of an operation and releases native sessions if [close] was
     * requested while the operation was running.
     */
    private fun endOperation() {
        synchronized(closeLock) {
            activeOperations--
            if (activeOperations == 0 && isCloseRequested) {
                releaseSessionsInternal()
            }
        }
    }

    /**
     * Ensures the given [bitmap] is backed by CPU-accessible memory so [Bitmap.getPixels]
     * and [Canvas.drawBitmap] succeed even if a `HARDWARE` bitmap was supplied.
     */
    private fun ensureSoftwareBitmap(bitmap: Bitmap): Pair<Bitmap, Boolean> {
        if (bitmap.config == Bitmap.Config.HARDWARE) {
            val softwareCopy = bitmap.copy(Bitmap.Config.ARGB_8888, false)
                ?: throw IllegalStateException("Failed to convert hardware bitmap to software bitmap.")
            return softwareCopy to true
        }
        return bitmap to false
    }

    /**
     * Synchronizes loaded ONNX sessions with the requested [modelGroupIds].
     * Closes unselected recognizers to free native RAM and loads only the missing ones.
     * Must be called inside [engineMutex].
     */
    private suspend fun loadModelsInternal(modelGroupIds: Set<String>) {
        // Force OrtEnvironment initialization before creating SessionOptions
        val ortEnv = env

        // Remove and close recognizers that are no longer selected
        val obsoleteIds = activeRecognizers.keys - modelGroupIds
        for (id in obsoleteIds) {
            activeRecognizers.remove(id)?.close()
        }

        val missingIds = modelGroupIds - activeRecognizers.keys
        if (detSession != null && missingIds.isEmpty()) return

        OrtSession.SessionOptions().use { options ->
            options.setIntraOpNumThreads(INTRA_OP_NUM_THREADS)

            for (modelId in missingIds) {
                val files = ocrModelRepository.getModelFiles(modelId)
                    ?: throw IllegalStateException("Model files for '$modelId' are not downloaded.")

                // Initialize the shared detector only once
                if (detSession == null) {
                    detSession = ortEnv.createSession(files.detectorFile.absolutePath, options)
                }

                val recSession = ortEnv.createSession(files.recognizerFile.absolutePath, options)
                val decoder = CtcLabelDecoder(files.dictionaryFile)
                activeRecognizers[modelId] = RecognizerPipeline(recSession, decoder)
            }
        }
    }

    /**
     * Crops, optionally rotates (if vertical), and scales a text region from [source] into a single
     * target [Bitmap] of height [REC_IMAGE_HEIGHT] using a single [Canvas] + [Matrix] pass.
     */
    private fun prepareCropForRecognition(
        source: Bitmap,
        minX: Int,
        minY: Int,
        width: Int,
        height: Int
    ): Bitmap {
        val isVertical = height > width * VERTICAL_TEXT_ASPECT_RATIO
        val effectiveWidth = if (isVertical) height else width
        val effectiveHeight = if (isVertical) width else height

        val aspectRatio = REC_IMAGE_HEIGHT.toFloat() / effectiveHeight
        val targetWidth = max(REC_IMAGE_HEIGHT, (effectiveWidth * aspectRatio).toInt())

        val targetBitmap = createBitmap(targetWidth, REC_IMAGE_HEIGHT)
        val matrix = Matrix().apply {
            // 1. Shift crop origin to (0, 0)
            postTranslate(-minX.toFloat(), -minY.toFloat())

            // 2. Rotate 90° counter-clockwise if vertical and shift back into positive coordinates
            if (isVertical) {
                postRotate(-90f)
                postTranslate(0f, width.toFloat())
            }

            // 3. Scale directly to target recognition dimensions
            postScale(
                targetWidth.toFloat() / effectiveWidth,
                REC_IMAGE_HEIGHT.toFloat() / effectiveHeight
            )
        }

        Canvas(targetBitmap).drawBitmap(source, matrix, bitmapPaint)
        return targetBitmap
    }

    /**
     * Evaluates a single cropped text tensor against all [recognizers] and returns the decoded
     * text and confidence score from whichever language model produced the highest confidence.
     */
    private fun recognizeBoxWithBestModel(
        recInputTensor: OnnxTensor,
        recognizers: List<RecognizerPipeline>
    ): Pair<String, Float> {
        var bestText = ""
        var bestConfidence = -1f

        for (recognizer in recognizers) {
            val inputName = recognizer.session.inputNames.first()
            recognizer.session.run(Collections.singletonMap(inputName, recInputTensor)).use { results ->
                val outputTensor = results[0] as OnnxTensor
                val shape = outputTensor.info.shape // Shape: [1, timeSteps, numClasses]
                val timeSteps = shape[1].toInt()
                val numClasses = shape[2].toInt()

                // Copy flat predictions directly from native buffer to avoid nested array allocations
                val floatBuffer = outputTensor.floatBuffer
                val flatPreds = FloatArray(floatBuffer.remaining())
                floatBuffer.get(flatPreds)

                val (text, confidence) = recognizer.decoder.decode(flatPreds, timeSteps, numClasses)
                if (confidence > bestConfidence) {
                    bestConfidence = confidence
                    bestText = text
                }
            }
        }

        return bestText to bestConfidence
    }

    /**
     * Groups bounding boxes into horizontal rows and sorts them top-to-bottom, and left-to-right
     * within each row. Uses strict single-property comparisons (`minY` then `minX`) to guarantee
     * `TimSort` transitivity.
     */
    private fun sortBoxesInReadingOrder(boxes: List<Box>): List<Box> {
        if (boxes.size <= 1) return boxes

        val sortedByY = boxes.sortedBy { it.minY }
        val rows = mutableListOf<MutableList<Box>>()

        for (box in sortedByY) {
            val currentRow = rows.lastOrNull()
            if (currentRow == null) {
                rows.add(mutableListOf(box))
            } else {
                val referenceBox = currentRow.first()
                val avgHeight = ((referenceBox.maxY - referenceBox.minY) + (box.maxY - box.minY)) / 2f
                val isSameRow = abs(box.minY - referenceBox.minY) < (avgHeight * NEW_LINE_HEIGHT_RATIO)
                if (isSameRow) {
                    currentRow.add(box)
                } else {
                    rows.add(mutableListOf(box))
                }
            }
        }

        return buildList(boxes.size) {
            for (row in rows) {
                row.sortBy { it.minX }
                addAll(row)
            }
        }
    }

    /**
     * Calculates the target dimensions for the detection model.
     * Ensures the longest side does not exceed [DET_MAX_SIDE_LEN] and rounds both dimensions
     * to the nearest multiple of [DET_STRIDE] (32), as required by PaddleOCR's neural network stride.
     */
    private fun calculateDetectionSize(width: Int, height: Int): Pair<Int, Int> {
        var resizeW = width.toFloat()
        var resizeH = height.toFloat()

        val ratio = max(resizeW / DET_MAX_SIDE_LEN, resizeH / DET_MAX_SIDE_LEN)
        if (ratio > 1f) {
            resizeW /= ratio
            resizeH /= ratio
        }

        val finalW = max(DET_STRIDE, (resizeW / DET_STRIDE).roundToInt() * DET_STRIDE)
        val finalH = max(DET_STRIDE, (resizeH / DET_STRIDE).roundToInt() * DET_STRIDE)
        return finalW to finalH
    }

    /**
     * Converts an Android [Bitmap] into a normalized NCHW (`[1, 3, H, W]`) [OnnxTensor]
     * backed by a direct native [ByteBuffer] to avoid extra JNI buffer copies.
     *
     * Note: The caller is responsible for closing the returned [OnnxTensor] (e.g., via `.use {}`).
     */
    private fun bitmapToNormalizedTensor(
        bitmap: Bitmap,
        mean: FloatArray,
        std: FloatArray
    ): OnnxTensor {
        val width = bitmap.width
        val height = bitmap.height
        val area = width * height
        val pixels = IntArray(area)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val floatBuffer = ByteBuffer.allocateDirect(3 * area * Float.SIZE_BYTES)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()

        for (i in 0 until area) {
            val pixel = pixels[i]
            val r = ((pixel shr 16 and 0xFF) / 255f - mean[0]) / std[0]
            val g = ((pixel shr 8 and 0xFF) / 255f - mean[1]) / std[1]
            val b = ((pixel and 0xFF) / 255f - mean[2]) / std[2]

            floatBuffer.put(i, r)
            floatBuffer.put(area + i, g)
            floatBuffer.put(2 * area + i, b)
        }

        val shape = longArrayOf(1, 3, height.toLong(), width.toLong())
        return OnnxTensor.createTensor(env, floatBuffer, shape)
    }

    /**
     * Extracts rectangular bounding boxes from the probability [heatMap] using a Breadth-First Search (BFS)
     * connected-components algorithm, then expands each box by [DET_UNCLIP_RATIO] to compensate for
     * shrunk text borders produced by segmentation detectors.
     */
    private fun findTextBoxes(heatMap: Array<FloatArray>): MutableList<Box> {
        if (heatMap.isEmpty() || heatMap[0].isEmpty()) return mutableListOf()

        val height = heatMap.size
        val width = heatMap[0].size
        val visited = Array(height) { BooleanArray(width) }
        val boxes = mutableListOf<Box>()
        val queue = ArrayDeque<Pair<Int, Int>>()

        for (y in 0 until height) {
            for (x in 0 until width) {
                if (heatMap[y][x] <= DET_PIXEL_THRESHOLD || visited[y][x]) continue

                var minX = x
                var maxX = x
                var minY = y
                var maxY = y
                var sumScore = 0f
                var pixelCount = 0

                queue.add(x to y)
                visited[y][x] = true

                while (queue.isNotEmpty()) {
                    val (cx, cy) = queue.removeFirst()
                    sumScore += heatMap[cy][cx]
                    pixelCount++

                    minX = min(minX, cx)
                    maxX = max(maxX, cx)
                    minY = min(minY, cy)
                    maxY = max(maxY, cy)

                    for (dir in 0..3) {
                        val nx = cx + NEIGHBOR_DX[dir]
                        val ny = cy + NEIGHBOR_DY[dir]
                        if (nx in 0 until width && ny in 0 until height && !visited[ny][nx]) {
                            if (heatMap[ny][nx] > DET_PIXEL_THRESHOLD) {
                                visited[ny][nx] = true
                                queue.add(nx to ny)
                            }
                        }
                    }
                }

                val avgScore = sumScore / pixelCount
                if (avgScore > DET_BOX_THRESHOLD) {
                    val boxW = maxX - minX
                    val boxH = maxY - minY
                    val expandW = (boxW * (DET_UNCLIP_RATIO - 1f)).toInt() / 2
                    val expandH = (boxH * (DET_UNCLIP_RATIO - 1f)).toInt() / 2

                    boxes.add(
                        Box(
                            minX = max(0, minX - expandW),
                            minY = max(0, minY - expandH),
                            maxX = min(width - 1, maxX + expandW),
                            maxY = min(height - 1, maxY + expandH),
                            score = avgScore
                        )
                    )
                }
            }
        }
        return boxes
    }

    /**
     * Releases all loaded ONNX model sessions from native memory, or defers release until
     * any in-flight inference finishes so native pointers are never freed while in use.
     * Does not close [env] because [OrtEnvironment] is a process-wide singleton.
     */
    override fun close() {
        synchronized(closeLock) {
            isCloseRequested = true
            if (activeOperations == 0) {
                releaseSessionsInternal()
            }
        }
    }

    /**
     * Closes the detector and all cached recognizer sessions immediately.
     * Must be called while holding [closeLock] with [activeOperations] equal to `0`.
     */
    private fun releaseSessionsInternal() {
        detSession?.close()
        detSession = null
        activeRecognizers.values.forEach { it.close() }
        activeRecognizers.clear()
    }

    companion object {
        /** Number of CPU threads per ONNX session to balance speed and thermal/battery efficiency on mobile. */
        private const val INTRA_OP_NUM_THREADS = 4

        /** Maximum length allowed for the longest side of the image during detection. */
        private const val DET_MAX_SIDE_LEN = 960f

        /** Required spatial multiple for PaddleOCR detection network dimensions. */
        private const val DET_STRIDE = 32

        /** Minimum probability for an individual pixel to be considered part of a text region. */
        private const val DET_PIXEL_THRESHOLD = 0.3f

        /** Minimum average probability required to keep a connected text region as a valid box. */
        private const val DET_BOX_THRESHOLD = 0.5f

        /** Expansion factor applied to detected boxes to include outer character edges. */
        private const val DET_UNCLIP_RATIO = 1.5f

        /** Fixed input height in pixels expected by the PaddleOCR recognition network. */
        private const val REC_IMAGE_HEIGHT = 48

        /** Minimum confidence score (0..1) required to accept a decoded text string. */
        private const val MIN_RECOGNITION_CONFIDENCE = 0.5f

        /** Height-to-width ratio above which a cropped box is treated as vertical text and rotated 90°. */
        private const val VERTICAL_TEXT_ASPECT_RATIO = 1.5f

        /** Vertical distance ratio relative to box height used to determine if two boxes belong to different lines. */
        private const val NEW_LINE_HEIGHT_RATIO = 0.5f

        /** Standard ImageNet RGB mean values used to normalize detection inputs. */
        private val IMAGENET_MEAN = floatArrayOf(0.485f, 0.456f, 0.406f)

        /** Standard ImageNet RGB standard deviation values used to normalize detection inputs. */
        private val IMAGENET_STD = floatArrayOf(0.229f, 0.224f, 0.225f)

        /** Mean values used to normalize recognition inputs into the `[-1, 1]` range. */
        private val REC_MEAN = floatArrayOf(0.5f, 0.5f, 0.5f)

        /** Standard deviation values used to normalize recognition inputs into the `[-1, 1]` range. */
        private val REC_STD = floatArrayOf(0.5f, 0.5f, 0.5f)

        /** Horizontal offsets for 4-directional neighbor traversal in BFS. */
        private val NEIGHBOR_DX = intArrayOf(-1, 1, 0, 0)

        /** Vertical offsets for 4-directional neighbor traversal in BFS. */
        private val NEIGHBOR_DY = intArrayOf(0, 0, -1, 1)
    }
}