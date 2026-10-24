package `in`.ignishers.milkmanager2.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

data class LanguageItem(
    val code: String,
    val nativeName: String,
    val englishName: String
)

object LocaleHelper {

    val SUPPORTED_LANGUAGES = listOf(
        LanguageItem(code = "en", nativeName = "English", englishName = "English"),
        LanguageItem(code = "hi", nativeName = "हिन्दी", englishName = "Hindi"),
        LanguageItem(code = "mr", nativeName = "मराठी", englishName = "Marathi"),
        LanguageItem(code = "gu", nativeName = "ગુજરાતી", englishName = "Gujarati"),
        LanguageItem(code = "pa", nativeName = "ਪੰਜਾਬੀ", englishName = "Punjabi"),
        LanguageItem(code = "bn", nativeName = "বাংলা", englishName = "Bengali"),
        LanguageItem(code = "te", nativeName = "తెలుగు", englishName = "Telugu")
    )

    fun setLocale(languageCode: String) {
        val appLocale = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    fun getSelectedLanguageCode(): String {
        val locales = AppCompatDelegate.getApplicationLocales()
        if (!locales.isEmpty) {
            val tag = locales.get(0)?.language
            if (!tag.isNullOrEmpty()) {
                val match = SUPPORTED_LANGUAGES.firstOrNull { it.code.equals(tag, ignoreCase = true) }
                if (match != null) return match.code
            }
        }
        val defaultLang = Locale.getDefault().language
        val match = SUPPORTED_LANGUAGES.firstOrNull { it.code.equals(defaultLang, ignoreCase = true) }
        return match?.code ?: "en"
    }

    fun getLanguageByCode(code: String): LanguageItem {
        return SUPPORTED_LANGUAGES.firstOrNull { it.code.equals(code, ignoreCase = true) }
            ?: SUPPORTED_LANGUAGES.first()
    }
}
