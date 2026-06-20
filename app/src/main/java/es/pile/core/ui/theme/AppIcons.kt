package es.pile.core.ui.theme

import androidx.annotation.DrawableRes
import es.pile.R

/**
 * Enumeration that contains the available icons for the app.
 *
 * @property id The unique identifier of the icon.
 * @property resourceId The resource ID of the icon.
 */
enum class AppIcons(
    val id: String,
    @DrawableRes val resourceId: Int
) {
    Add("Add", R.drawable.ic_add_24px),
    Bank("Bank", R.drawable.user_ic_account_balance_24px),
    Id("Id", R.drawable.user_ic_badge_24px),
    Starred("Starred", R.drawable.user_ic_bookmark_star_24px),
    Category("Category", R.drawable.user_ic_category_24px),
    Dentistry("Dentistry", R.drawable.user_ic_dentistry_24px),
    Medical("Medical", R.drawable.user_ic_medical_services_24px),
    Pet("Pet", R.drawable.user_ic_pets_24px),
    Receipts("Receipts", R.drawable.user_ic_receipt_24px),
    Bills("Bills", R.drawable.user_ic_request_page_24px),
    Legal("Legal", R.drawable.user_ic_things_to_do_24px),
    Insurance("Insurance", R.drawable.user_ic_umbrella_24px),
    Mail("Mail", R.drawable.mail_24px),
    Unknow("Unknow", R.drawable.warning_24px);

    companion object {
        /**
         * Returns the resource ID of the icon with the given ID.
         *
         * @param id The ID of the icon.
         * @return The resource ID of the icon or [Unknow] if the ID is not found.
         */
        fun getById(id: String): Int {
            return (entries.find { it.id == id } ?: Unknow).resourceId
        }
    }
}