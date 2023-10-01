import org.nedz.bughunting.engine.Analyzer
import org.nedz.bughunting.rules.UnusedImportRule
import java.io.File

fun main() {

    val analyzer = Analyzer(listOf(

    ))

    val classpath = System.getProperty("java.class.path").split(System.getProperty("path.separator"))
    val testFile1 = "src/main/resources/package1/file1.kt"
    val testFile2 = "src/main/resources/package2/file2.kt"

    println(analyzer.analyze(listOf(File(testFile1), File(testFile2)), classpath))

}