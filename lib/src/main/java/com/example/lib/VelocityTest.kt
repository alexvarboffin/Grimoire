import org.apache.velocity.Template
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import java.io.StringWriter


fun main(args: Array<String>) {
    testtest(args)
}

fun testtest(args: Array<String>) {

    val velocityEngine = VelocityEngine()
    velocityEngine.init()

    val t: Template = velocityEngine.getTemplate("index.vm")

    val context = VelocityContext()
    context.put("name", "World")

    val writer = StringWriter()
    t.merge(context, writer)

}
