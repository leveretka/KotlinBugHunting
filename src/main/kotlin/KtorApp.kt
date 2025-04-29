package org.nedz.bughunting

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.html.*
import kotlinx.html.*
import org.nedz.bughunting.engine.Analyzer
import org.nedz.bughunting.rules.CustomRule
import java.io.File
import kotlin.io.path.createTempDirectory

/**
 * Start the Ktor application
 */
fun startKtorApp() {
    embeddedServer(Netty, port = 8080) {
        configureRouting()
    }.start(wait = true)
}

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondHtml(HttpStatusCode.OK) {
                head {
                    title("Kotlin Bug Hunter")
                    style {
                        +"""
                            body { font-family: Arial, sans-serif; margin: 20px; }
                            .container { max-width: 1200px; margin: 0 auto; }
                            textarea { width: 100%; height: 300px; font-family: monospace; }
                            .context-files { margin-top: 20px; }
                            .submit-button { margin-top: 20px; }
                            .highlighted-issue { background-color: #ffcccc; }
                        """
                    }
                }
                body {
                    div(classes = "container") {
                        h1 { +"Kotlin Bug Hunter" }

                        form(action = "/analyze", method = FormMethod.post) {
                            div {
                                h3 { +"Kotlin Code" }
                                textArea {
                                    name = "code"
                                    +"""
                                        // Paste your Kotlin code here
                                        import java.util.ArrayList
                                        import kotlin.collections.List

                                        fun main() {
                                            val list = listOf(1, 2, 3)
                                            println(list)
                                        }
                                    """.trimIndent()
                                }
                            }

                            div(classes = "context-files") {
                                h3 { +"Context Files (Optional)" }
                                p { +"Enter paths to other Kotlin files for context, one per line:" }
                                textArea {
                                    name = "contextFiles"
                                    +"src/main/resources/package1/file1.kt\nsrc/main/resources/package2/file2.kt"
                                }
                            }

                            div(classes = "submit-button") {
                                submitInput { value = "Analyze" }
                            }
                        }
                    }
                }
            }
        }

        post("/analyze") {
            val parameters = call.receiveParameters()
            val code = (parameters["code"] ?: "").replace("\r\n", "\n")
            val contextFilesText = parameters["contextFiles"] ?: ""

            // Create a temporary directory for the input file
            val tempDir = createTempDirectory("kotlin-bug-hunter")
            val inputFile = tempDir.resolve("input.kt").toFile()
            inputFile.writeText(code)

            // Parse context files
            val contextFilePaths = contextFilesText
                .split("\n")
                .filter { it.isNotBlank() }
                .map { File(it.trim()) }
                .filter { it.exists() }

            // Add the input file to the list of files to analyze
            val filesToAnalyze = listOf(inputFile) + contextFilePaths

            // Run the analyzer
            val analyzer = Analyzer(listOf(CustomRule()))
            val classpath = System.getProperty("java.class.path").split(System.getProperty("path.separator"))
            val issues = analyzer.analyze(filesToAnalyze, classpath)

            // Filter issues to only include those from the input file
            val inputFileIssues = issues.filter { it.file == inputFile.absolutePath }

            // Highlight issues in the code
            val codeLines = code.lines()
            val highlightedCode = StringBuilder()

            for ((lineIndex, line) in codeLines.withIndex()) {
                val lineNumber = lineIndex + 1
                val lineIssues = inputFileIssues.filter { 
                    it.location.startLine <= lineNumber && it.location.endLine >= lineNumber 
                }

                if (lineIssues.isEmpty()) {
                    // No issues on this line, just add the escaped line
                    highlightedCode.append(line.replace("<", "&lt;").replace(">", "&gt;"))
                } else {
                    // We have issues on this line, highlight the specific parts
                    val escapedLine = line.replace("<", "&lt;").replace(">", "&gt;")

                    // Create a list of segments to highlight
                    data class Segment(val start: Int, val end: Int, val message: String)
                    val segments = mutableListOf<Segment>()

                    for (issue in lineIssues) {
                        val startCol = if (issue.location.startLine == lineNumber) issue.location.startColumn - 1 else 0
                        val endCol = if (issue.location.endLine == lineNumber) issue.location.endColumn - 1 else escapedLine.length
                        segments.add(Segment(startCol, endCol, issue.message))
                    }

                    // Sort segments by start position
                    segments.sortBy { it.start }

                    // Merge overlapping segments
                    val mergedSegments = mutableListOf<Segment>()
                    var currentSegment: Segment? = null

                    for (segment in segments) {
                        if (currentSegment == null) {
                            currentSegment = segment
                        } else if (segment.start <= currentSegment.end) {
                            // Segments overlap, merge them
                            currentSegment = Segment(
                                currentSegment.start,
                                maxOf(currentSegment.end, segment.end),
                                currentSegment.message + "; " + segment.message
                            )
                        } else {
                            // No overlap, add current segment and start a new one
                            mergedSegments.add(currentSegment)
                            currentSegment = segment
                        }
                    }

                    if (currentSegment != null) {
                        mergedSegments.add(currentSegment)
                    }

                    // Build the highlighted line
                    var lastEnd = 0
                    for (segment in mergedSegments) {
                        // Add text before the segment
                        if (segment.start > lastEnd) {
                            highlightedCode.append(escapedLine.substring(lastEnd, segment.start))
                        }

                        // Add the highlighted segment
                        highlightedCode.append("<span class=\"highlighted-issue\" title=\"")
                        highlightedCode.append(segment.message)
                        highlightedCode.append("\">")
                        highlightedCode.append(escapedLine.substring(segment.start, segment.end))
                        highlightedCode.append("</span>")

                        lastEnd = segment.end
                    }

                    // Add any remaining text after the last segment
                    if (lastEnd < escapedLine.length) {
                        highlightedCode.append(escapedLine.substring(lastEnd))
                    }
                }
                highlightedCode.append("\n")
            }

            // Clean up temporary files
            inputFile.delete()
            tempDir.toFile().delete()

            call.respondHtml(HttpStatusCode.OK) {
                head {
                    title("Analysis Results - Kotlin Bug Hunter")
                    style {
                        +"""
                            body { font-family: Arial, sans-serif; margin: 20px; }
                            .container { max-width: 1200px; margin: 0 auto; }
                            pre { background-color: #f5f5f5; padding: 10px; overflow-x: auto; }
                            .highlighted-issue { background-color: #ffcccc; cursor: help; }
                            .issues-list { margin-top: 20px; }
                            .issue-item { margin-bottom: 10px; padding: 10px; background-color: #f9f9f9; }
                            .back-button { margin-top: 20px; }
                        """
                    }
                }
                body {
                    div(classes = "container") {
                        h1 { +"Analysis Results" }

                        h2 { +"Code with Issues Highlighted" }
                        pre {
                            unsafe {
                                +highlightedCode.toString()
                            }
                        }

                        div(classes = "issues-list") {
                            h2 { +"Issues Found (${inputFileIssues.size})" }
                            if (inputFileIssues.isEmpty()) {
                                p { +"No issues found!" }
                            } else {
                                inputFileIssues.forEachIndexed { index, issue ->
                                    div(classes = "issue-item") {
                                        h3 { +"Issue #${index + 1}: ${issue.ruleName}" }
                                        p { +issue.message }
                                        p { +"Location: ${issue.location.startLine}:${issue.location.startColumn} - ${issue.location.endLine}:${issue.location.endColumn}" }
                                    }
                                }
                            }
                        }

                        div(classes = "back-button") {
                            a(href = "/") { +"Back to Analyzer" }
                        }
                    }
                }
            }
        }
    }
}
