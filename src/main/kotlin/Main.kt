import org.nedz.bughunting.startKtorApp

/**
 * Print usage information.
 */
fun printUsage() {
    println("""
        Usage: java -jar KotlinBugHunting.jar [option]

        Options:
          --server            Start the Ktor server (default)
          --help              Show this help message

        If no option is provided, the server will be started by default.

        Examples:
          java -jar KotlinBugHunting.jar
          java -jar KotlinBugHunting.jar --server
    """.trimIndent())
}

/**
 * Main function with command-line argument parsing.
 */
fun main(args: Array<String>) {
    when {
        args.isEmpty() -> startKtorApp()
        args.contains("--server") -> startKtorApp()
        args.contains("--help") -> printUsage()
        else -> {
            println("Unknown option: ${args.joinToString(" ")}")
            printUsage()
        }
    }
}
