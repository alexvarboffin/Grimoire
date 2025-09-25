package domain.templates

import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import java.io.StringWriter

actual fun renderTemplate(templateContent: String, variables: Map<String, String>): String {
    val ve = VelocityEngine()
    ve.init()

    val context = VelocityContext()
    variables.forEach { (key, value) ->
        context.put(key, value)
    }

    val writer = StringWriter()
    ve.evaluate(context, writer, "template", templateContent)

    return writer.toString()
}
