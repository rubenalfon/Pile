package com.ganadoro.pile.di

import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.ganadoro.pile.Database
import com.ganadoro.pile.DatabaseQueries
import com.ganadoro.pile.DocumentModel
import com.ganadoro.pile.models.DocumentDetail
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.time.LocalDate


val databaseModule = module {
    single<Database> {
        Database(
            driver = get(),
            DocumentModelAdapter = DocumentModel.Adapter(
                creationDateAdapter = get(named("LocalDateStringAdapter")),
                modificationDateAdapter = get(named("LocalDateStringAdapter")),
                documentPileIdsAdapter = get(named("StringListAdapter")),
                documentDetailsAdapter = get(named("DocumentDetailListAdapter")),
                documentOrganizationIdsAdapter = get(named("StringListAdapter"))

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

    single<ColumnAdapter<LocalDate, String>>(named("LocalDateStringAdapter")) {
        object : ColumnAdapter<LocalDate, String> {
            override fun decode(databaseValue: String): LocalDate =
                if (databaseValue.isEmpty()) {
                    LocalDate.now()
                } else {
                    LocalDate.parse(databaseValue)
                }

            override fun encode(value: LocalDate): String = value.toString()
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
}