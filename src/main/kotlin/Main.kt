import org.nedz.bughunting.engine.Analyzer
import org.nedz.bughunting.rules.UnusedImport1
import org.nedz.bughunting.rules.UnusedImport2
import org.nedz.bughunting.rules.UnusedImport3
import org.nedz.bughunting.rules.UnusedImport4
import java.io.File

fun main() {

    val analyzer = Analyzer(listOf(
        //UnusedImport1(),
        //UnusedImport2(),
        UnusedImport3(),
        UnusedImport4(),
    ))

    val classpath = System.getProperty("java.class.path").split(System.getProperty("path.separator"))
    val testFile1 = "src/main/resources/package1/HelloWorld.kt"
    val testFil2 = "src/main/resources/package2/MyCLass.kt"


    println(analyzer.analyze(listOf(File(testFile1)), classpath))

}