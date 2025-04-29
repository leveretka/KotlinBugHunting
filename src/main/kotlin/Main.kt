import org.nedz.bughunting.engine.Analyzer
import org.nedz.bughunting.rules.UnusedImportRule
import org.nedz.bughunting.rules.util.UnusedImport1
import java.io.File
import com.google.gson.GsonBuilder

fun main() {

    val analyzer = Analyzer(listOf(
        UnusedImport1()
    ))

    val gson = GsonBuilder().setPrettyPrinting().create()

    val classpath = System.getProperty("java.class.path").split(System.getProperty("path.separator"))
    val testFile1 = "src/main/resources/package1/file1.kt"
    val testFile2 = "src/main/resources/package2/file2.kt"

    val issues = analyzer.analyze(listOf(File(testFile1), File(testFile2)), classpath)

    val jsonOutput = gson.toJson(issues)
    println(jsonOutput)

}
