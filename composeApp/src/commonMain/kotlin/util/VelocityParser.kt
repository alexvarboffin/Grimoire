package util

object VelocityParser {
    private val velocityVariableRegex = """\$!?\{?([a-zA-Z_][a-zA-Z0-9_]*)\}?""".toRegex()

    fun extractVariables(templateContent: String): Set<String> {
        return velocityVariableRegex.findAll(templateContent)
            .map { it.groupValues[1] }
            .toSet()
    }
}
