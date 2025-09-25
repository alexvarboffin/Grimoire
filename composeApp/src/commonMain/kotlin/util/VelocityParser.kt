package util

object VelocityParser {
    private val velocityVariableRegex = """\$!?\{?([a-zA-Z_][a-zA-Z0-9_]*)\}?""".toRegex()

    fun extractVariables(templateContent: String): Set<String> {
        return velocityVariableRegex.findAll(templateContent)
            .map { it.groupValues[1] }
            .toSet()
    }

    private val defaultValRegex = """##\s*@default\(([^=]+)="([^"]+)"\)""".toRegex()

    fun extractDefaults(templateContent: String): Map<String, String> {
        return defaultValRegex.findAll(templateContent).associate {
            val (key, value) = it.destructured
            key.trim() to value
        }
    }

    private val filenameRegex = """##\s*@filename\("([^"]+)"\)""".toRegex()

    fun extractFilename(templateContent: String): String? {
        return filenameRegex.find(templateContent)?.groupValues?.get(1)
    }

    private val listVarRegex = """##\s*@list\(([^)]+)\)""".toRegex()

    fun extractListVariableNames(templateContent: String): List<String> {
        return listVarRegex.findAll(templateContent).map { it.groupValues[1].trim() }.toList()
    }
}
