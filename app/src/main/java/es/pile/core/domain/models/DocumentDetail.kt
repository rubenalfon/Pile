package es.pile.core.domain.models

import kotlinx.serialization.Serializable

@Serializable
sealed interface DocumentDetail {
    val id: String
    val name: String
}
@Serializable
data class StringDetail(
    override val id: String,
    override val name: String,
    val value: String
) : DocumentDetail

// Possible update ;)
//@Serializable
//data class FloatDetail(
//    override val id: String,
//    override val name: String,
//    val value: Float
//) : DocumentDetail

//@Serializable
//data class DateDetail(
//    override val name: String,
//    val value: LocalDate
//) : DocumentDetail
