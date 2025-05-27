package com.ganadoro.pile.models

import java.time.LocalDate

sealed interface DocumentDetail {
    val name: String
}

data class StringDetail(
    override val name: String,
    val value: String
) : DocumentDetail

data class FloatDetail(
    override val name: String,
    val value: Float
) : DocumentDetail

data class DateDetail(
    override val name: String,
    val value: LocalDate
) : DocumentDetail
