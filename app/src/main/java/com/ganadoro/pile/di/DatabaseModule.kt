package com.ganadoro.pile.di

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.ui.graphics.vector.ImageVector
import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.ganadoro.pile.Database
import com.ganadoro.pile.DatabaseQueries
import com.ganadoro.pile.PileModel
import com.ganadoro.pile.util.AppIcon
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.time.LocalDate

val databaseModule = module {
    single<Database> {
        Database(
            driver = get(),
            PileModelAdapter = PileModel.Adapter(
                iconAdapter = get(named("ImageVectorStringAdapter"))
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

    single<ColumnAdapter<ImageVector, String>>(named("ImageVectorStringAdapter")) {
        object : ColumnAdapter<ImageVector, String> {
            override fun decode(databaseValue: String): ImageVector =
                runCatching { AppIcon.valueOf(databaseValue).imageVector }
                    .getOrElse { Icons.AutoMirrored.Filled.Help }

            override fun encode(value: ImageVector): String = value.name.substringAfter(".")
        }
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
}