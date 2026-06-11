package es.pile.core.ui.util

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

/**
 * A sealed class that represents text to be displayed in the UI.
 * 
 * It allows the ViewModel to decide which text should be shown (either a hardcoded string 
 * or a localized resource) without needing a reference to the Android [Context]. 
 * The actual string resolution is deferred until the UI layer.
 */
sealed class UiText {

    /**
     * Represents a raw [String] that doesn't need localization (e.g., error from an API).
     */
    data class DynamicString(val value: String) : UiText()

    /**
     * Represents a localized string from Android resources.
     * 
     * @property resId The string resource identifier (R.string.example).
     * @property args Optional arguments for string formatting (e.g., "Page %d").
     */
    class StringResource(
        @StringRes val resId: Int,
        vararg val args: Any
    ) : UiText()

    /**
     * Resolves the [UiText] into a [String] within a Composable function.
     * Uses the internal [stringResource] helper.
     */
    @Composable
    fun asString(): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> stringResource(resId, *args)
        }
    }

    /**
     * Resolves the [UiText] into a [String] using a standard [Context].
     * Useful for showing Snapbars or Toasts in non-composable scopes.
     */
    fun asString(context: Context): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> context.getString(resId, *args)
        }
    }
}
