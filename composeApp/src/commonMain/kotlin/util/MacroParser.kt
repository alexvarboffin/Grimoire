package util

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

object MacroParser {
    // Регулярка для поиска динамических вводов {?prompt}
    val DYNAMIC_MACRO_REGEX = """\{\?\w+\}""".toRegex()

    fun parse(
        text: String, 
        projectRoot: File?, 
        globalVars: Map<String, String> = emptyMap(),
        userInputs: Map<String, String> = emptyMap()
    ): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val date = SimpleDateFormat("yyyy-MM-dd").format(Date())
        val userHome = System.getProperty("user.home")
        
        var result = text
            .replace("{timestamp}", timestamp)
            .replace("{date}", date)
            .replace("{user_home}", userHome)
            .replace("{project_root}", projectRoot?.absolutePath ?: "")
            .replace("{temp_dir}", System.getProperty("java.io.tmpdir"))

        // Подставляем глобальные переменные
        globalVars.forEach { (key, value) ->
            result = result.replace("{$key}", value)
        }

        // Подставляем вводы пользователя {?prompt}
        userInputs.forEach { (key, value) ->
            result = result.replace("{?$key}", value)
        }

        return result
    }

    fun extractDynamicMacros(text: String): List<String> {
        return DYNAMIC_MACRO_REGEX.findAll(text).map { it.value.removePrefix("{?").removeSuffix("}") }.toList()
    }
}