package org.nedz.bughunting.testing

import org.nedz.bughunting.api.Rule
import org.nedz.bughunting.engine.Analyzer
import org.nedz.bughunting.utils.IssueCommentUtils
import java.io.File

/**
 * A simple testing framework for verifying rule functionality.
 */
class TestFramework {

    /**
     * Checks if the issues found by the analyzer match the issues specified in the comments.
     * Issue comments are in the format:
     * // ISSUE|RuleName|Message|StartLine:StartColumn-EndLine:EndColumn
     * 
     * @param filePath Path to the file to check
     * @param rules List of rules to apply (defaults to all available rules)
     * @return TestResult containing success/failure status and details
     */
    fun checkFileWithComments(filePath: String, rules: List<Rule> = listOf(
        org.nedz.bughunting.rules.CustomRule(),
        org.nedz.bughunting.rules.MutableCollectionRule()
    )): TestResult {
        val file = File(filePath)
        if (!file.exists()) {
            return TestResult(false, "File not found: $filePath")
        }

        // Parse issue comments from the file
        val expectedIssues = IssueCommentUtils.parseIssueComments(file)

        // Run the analyzer
        val classpath = System.getProperty("java.class.path").split(File.pathSeparator)
        val analyzer = Analyzer(rules)
        val actualIssues = analyzer.analyze(listOf(file), classpath)

        // Compare expected and actual issues
        val (missingIssues, unexpectedIssues) = IssueCommentUtils.compareIssues(expectedIssues, actualIssues)

        val success = missingIssues.isEmpty() && unexpectedIssues.isEmpty()
        val details = buildString {
            if (!success) {
                if (missingIssues.isNotEmpty()) {
                    appendLine("Missing issues:")
                    missingIssues.forEach { appendLine("  - $it") }
                }
                if (unexpectedIssues.isNotEmpty()) {
                    appendLine("Unexpected issues:")
                    unexpectedIssues.forEach { appendLine("  - $it") }
                }
            } else {
                appendLine("All ${expectedIssues.size} expected issues were found.")
            }
        }

        return TestResult(success, details)
    }

}

/**
 * Represents an expected issue in a test file.
 */
data class ExpectedIssue(
    val ruleName: String,
    val message: String = "",
    val line: Int,
    val column: Int? = null,
    val length: Int? = null
)

/**
 * Represents the result of a test.
 */
data class TestResult(
    val success: Boolean,
    val details: String
)

/**
 * DSL for building expected issues.
 */
class ExpectedIssueBuilder {
    private val issues = mutableListOf<ExpectedIssue>()

    fun expect(ruleName: String, line: Int, column: Int? = null, length: Int? = null, message: String = "") {
        issues.add(ExpectedIssue(ruleName, message, line, column, length))
    }

    fun build(): List<ExpectedIssue> = issues.toList()
}

/**
 * DSL function to create expected issues.
 */
fun expectedIssues(init: ExpectedIssueBuilder.() -> Unit): List<ExpectedIssue> {
    val builder = ExpectedIssueBuilder()
    builder.init()
    return builder.build()
}
