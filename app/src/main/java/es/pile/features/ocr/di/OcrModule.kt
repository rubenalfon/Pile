package es.pile.features.ocr.di

import es.pile.features.ocr.data.engine.OnnxOcrEngine
import es.pile.features.ocr.data.schedulers.OcrServiceImpl
import es.pile.features.ocr.data.workers.OcrWorker
import es.pile.features.ocr.data.workers.PendingOcrWorker
import es.pile.features.ocr.domain.schedulers.OcrService
import es.pile.features.ocr.domain.usecases.UpdateDocumentOcrTextUseCase
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val ocrModule = module {
    single { OnnxOcrEngine(get()) }

    workerOf(::OcrWorker)
    workerOf(::PendingOcrWorker)

    single<OcrService> { OcrServiceImpl(get()) }

    factoryOf(::UpdateDocumentOcrTextUseCase)
}
