package util

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

object MacroParser {
    fun parse(text: String, projectRoot: File?): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val date = SimpleDateFormat("yyyy-MM-dd").format(Date())
        val userDir = System.getProperty("user.home")
        
        return text
            .replace("{timestamp}", timestamp)
            .replace("{date}", date)
            .replace("{user_home}", userDir)
            .replace("{project_root}", projectRoot?.absolutePath ?: "")
            .replace("{temp_dir}", System.getProperty("java.io.tmpdir"))
    }
}
