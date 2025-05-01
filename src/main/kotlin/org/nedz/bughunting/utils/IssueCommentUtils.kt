package org.nedz.bughunting.utils

import org.nedz.bughunting.api.Issue
import org.nedz.bughunting.api.Location
import java.io.File

/**
 * Utility class for parsing and working with issue comments in example files.
 * Issue comments are in the format:
 * // ISSUE|RuleName|Message|StartLine:StartColumn-EndLine:EndColumn
 */
object IssueCommentUtils {

    private val ISSUE_COMMENT_REGEX = """//\s*ISSUE\|([^|]+)\|([^|]+)\|(\d+):(\d+)-(\d+):(\d+)""".toRegex()

    /**
     * Parse issue comments from a file.
     * 
     * @param file The file to parse
     * @return A list of issues parsed from the comments
     */
    fun parseIssueComments(file: File): List<Issue> {
        val issues = mutableListOf<Issue>()
        val lines = file.readLines()

        lines.forEachIndexed { _, line ->
            val matchResult = ISSUE_COMMENT_REGEX.find(line)
            if (matchResult != null) {
                val (ruleName, message, startLine, startColumn, endLine, endColumn) = matchResult.destructured

                issues.add(
                    Issue(
                        file = file.absolutePath,
                        ruleName = ruleName.trim(),
                        message = message.trim(),
                        location = Location(
                            startLine = startLine.toInt(),
                            startColumn = startColumn.toInt(),
                            endLine = endLine.toInt(),
                            endColumn = endColumn.toInt()
                        )
                    )
                )
            }
        }

        return issues
    }

    /**
     * Format an issue as a comment.
     * 
     * @param issue The issue to format
     * @return The issue formatted as a comment
     */
    fun formatIssueComment(issue: Issue): String {
        return "// ISSUE|${issue.ruleName}|${issue.message}|${issue.location.startLine}:${issue.location.startColumn}-${issue.location.endLine}:${issue.location.endColumn}"
    }

    /**
     * Compare two lists of issues.
     * 
     * @param expectedIssues The expected issues
     * @param actualIssues The actual issues
     * @return A pair of lists: missing issues and unexpected issues
     */
    fun compareIssues(expectedIssues: List<Issue>, actualIssues: List<Issue>): Pair<List<Issue>, List<Issue>> {
        val missingIssues = expectedIssues.filter { expected -> !actualIssues.any { actual -> actual == expected } }
        val unexpectedIssues = actualIssues.filter { actual -> !expectedIssues.any { expected -> expected == actual } }
        return missingIssues to unexpectedIssues
    }
}
