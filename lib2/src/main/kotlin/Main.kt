import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import java.io.FileReader
import java.io.FileWriter
import java.io.StringWriter
import java.io.File

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Usage: java -jar lib2.jar <template_path> [-o <output_dir>] [ext=<extension>] [p=<packageName1>,<packageName2>,...] [key=value] ...")
        return
    }
    //
    val templatePath = args[0]
    var outputDir = "."
    var extension = "java"
    var packageNames = listOf<String>()
    val variables = mutableMapOf<String, Any>()

    var i = 1
    while (i < args.size) {
        when {
            args[i] == "-o" || args[i] == "--output" -> {
                if (i + 1 < args.size) {
                    outputDir = args[i + 1]
                    i += 2
                } else {
                    println("Error: Missing output directory path.")
                    return
                }
            }
            args[i].startsWith("ext=") -> {
                extension = args[i].substring(4)
                i++
            }
            args[i].startsWith("p=") -> {
                packageNames = args[i].substring(2).split(",")
                i++
            }
            else -> {
                val parts = args[i].split("=", limit = 2)
                if (parts.size == 2) {
                    variables[parts[0]] = parts[1]
                }
                i++
            }
        }
    }

    try {
        val ve = VelocityEngine()
        ve.init()

        if (packageNames.isNotEmpty()) {
            for (packageName in packageNames) {
                val context = VelocityContext()
                variables.forEach { (key, value) ->
                    context.put(key, value)
                }
                context.put("packageName", packageName)

                val reader = FileReader(templatePath)
                val writer = StringWriter()
                ve.evaluate(context, writer, "template", reader)

                val outputDirFile = File(outputDir)
                outputDirFile.mkdirs()

                val fileName = packageName.split(".").last().replaceFirstChar { it.uppercase() } + "." + extension
                val outputFile = File(outputDirFile, fileName)

                val fileWriter = FileWriter(outputFile)
                fileWriter.write(writer.toString())
                fileWriter.close()

                println("File generated successfully at: ${outputFile.absolutePath}")
            }
        } else {
            val context = VelocityContext()
            variables.forEach { (key, value) ->
                context.put(key, value)
            }

            val reader = FileReader(templatePath)
            val writer = StringWriter()
            ve.evaluate(context, writer, "template", reader)

            println(writer.toString())
        }

    } catch (e: Exception) {
        println("Error: ${e.message}")
    }
}
