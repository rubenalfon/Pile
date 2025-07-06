package com.ganadoro.pile.util

import androidx.annotation.DrawableRes
import com.ganadoro.pile.R


data class SelectableIcon(val id: String, @DrawableRes val nameResId: Int)

object IconPack {
    val icons = listOf(
        SelectableIcon("Add", R.drawable.ic_add_24px),
        SelectableIcon("Bank", R.drawable.user_ic_account_balance_24px),
        SelectableIcon("Id", R.drawable.user_ic_badge_24px),
        SelectableIcon("Starred", R.drawable.user_ic_bookmark_star_24px),
        SelectableIcon("Category", R.drawable.user_ic_category_24px),
        SelectableIcon("Dentistry", R.drawable.user_ic_dentistry_24px),
        SelectableIcon("Medical", R.drawable.user_ic_medical_services_24px),
        SelectableIcon("Pet", R.drawable.user_ic_pets_24px),
        SelectableIcon("Receipts", R.drawable.user_ic_receipt_24px),
        SelectableIcon("Bills", R.drawable.user_ic_request_page_24px),
        SelectableIcon("Legal", R.drawable.user_ic_things_to_do_24px),
        SelectableIcon("Insurance", R.drawable.user_ic_umbrella_24px),
    )

    fun getSelectableIcon(name: String): SelectableIcon? {
        return icons.find { it.id == name }
    }

    fun getIcon(name: String): Int? {
        return icons.find { it.id == name }?.nameResId
    }
}