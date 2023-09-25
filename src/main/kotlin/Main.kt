import org.jetbrains.kotlin.psi.KtNamedFunction
import org.nedz.bughunting.api.FileContext
import org.nedz.bughunting.api.Rule
import org.nedz.bughunting.engine.Analyzer
import java.io.File

fun main() {

    val analyzer = Analyzer(listOf(
        object : Rule("My rule") {
            override fun visitNamedFunction(function: KtNamedFunction, ctx: FileContext) {
                ctx.addIssue("Wrong function!", function)
            }

        }
    ))

    val classpath = System.getProperty("java.class.path").split(System.getProperty("path.separator"))
    val testFile = "/Users/margarita/Projects/KotlinBugHunting/src/main/resources/HelloWorld.kt"

    println(analyzer.analyze(listOf(File(testFile)), classpath))



}