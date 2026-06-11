package es.pile.core.di

import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import es.pile.Database
import es.pile.DatabaseQueries
import es.pile.DocumentImage
import es.pile.DocumentModel
import es.pile.core.domain.models.DocumentDetail
import es.pile.core.domain.models.DocumentStatus
import es.pile.core.domain.models.ImageCropData
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val databaseModule = module {
    single<Database> {
        Database(
            driver = get(),
            DocumentModelAdapter = DocumentModel.Adapter(
                imageIdsAdapter = get(named("StringListAdapter")),
                creationDateTimeAdapter = get(named("LocalDateTimeStringAdapter")),
                modificationDateTimeAdapter = get(named("LocalDateTimeStringAdapter")),
                documentStatusAdapter = get(named("DocumentStatusAdapter")),
                documentPileIdsAdapter = get(named("StringListAdapter")),
                documentDetailsAdapter = get(named("DocumentDetailListAdapter")),
                documentOrganizationIdsAdapter = get(named("StringListAdapter"))
            ),
            DocumentImageAdapter = DocumentImage.Adapter(
                cropAdapter = get(named("ImageCropDataAdapter"))
            )
        )
    }

    single<SqlDriver> {
        AndroidSqliteDriver(
            schema = Database.Schema,
            context = get(),
            name = "pile.db"

        )
    }

    single<DatabaseQueries> {
        get<Database>().databaseQueries
    }

    single<ColumnAdapter<LocalDateTime, String>>(named("LocalDateTimeStringAdapter")) {
        object : ColumnAdapter<LocalDateTime, String> {
            override fun decode(databaseValue: String): LocalDateTime =
                try {
                    LocalDateTime.parse(databaseValue)
                } catch (_: Exception) {
                    LocalDateTime.now()
                }

            override fun encode(value: LocalDateTime): String {
                return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(value)
            }
        }
    }

    single<ColumnAdapter<List<String>, String>>(named("StringListAdapter")) {
        object : ColumnAdapter<List<String>, String> {
            override fun decode(databaseValue: String): List<String> {
                return Json.decodeFromString(databaseValue)
            }

            override fun encode(value: List<String>): String {
                return Json.encodeToString(value)
            }
        }
    }

    single<ColumnAdapter<List<DocumentDetail>, String>>(named("DocumentDetailListAdapter")) {
        object : ColumnAdapter<List<DocumentDetail>, String> {
            override fun decode(databaseValue: String): List<DocumentDetail> {
                return Json.decodeFromString(databaseValue)
            }

            override fun encode(value: List<DocumentDetail>): String {
                return Json.encodeToString(value)
            }
        }
    }

    single<ColumnAdapter<ImageCropData, String>>(named("ImageCropDataAdapter")) {
        object : ColumnAdapter<ImageCropData, String> {
            override fun decode(databaseValue: String): ImageCropData {
                return Json.decodeFromString(databaseValue)
            }

            override fun encode(value: ImageCropData): String {
                return Json.encodeToString(value)
            }
        }
    }

    single<ColumnAdapter<DocumentStatus, Long>>(named("DocumentStatusAdapter")) {
        object : ColumnAdapter<DocumentStatus, Long> {
            override fun decode(databaseValue: Long): DocumentStatus {
                return databaseValue.toInt()
            }

            override fun encode(value: DocumentStatus): Long {
                return value.toLong()
            }
        }
    }
}