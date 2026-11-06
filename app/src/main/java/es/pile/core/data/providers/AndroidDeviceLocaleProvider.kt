package es.pile.core.data.providers

import androidx.core.os.LocaleListCompat
import es.pile.core.domain.providers.DeviceLocaleProvider

class AndroidDeviceLocaleProvider : DeviceLocaleProvider {
    override fun getLanguageCodes(): List<String> {
        val locales = LocaleListCompat.getAdjustedDefault()
        val languages = mutableSetOf<String>()

        for (i in 0 until locales.size()) {
            locales.get(i)?.language?.let { languages.add(it) }
        }

        return languages.toList()
    }
}
