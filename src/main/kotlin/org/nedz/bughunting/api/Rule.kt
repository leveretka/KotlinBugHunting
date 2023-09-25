package org.nedz.bughunting.api

import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtVisitor

open class Rule(val name: String) : KtVisitor<Unit, FileContext>() {

    fun FileContext.addIssue(message: String, ktElement: KtElement) {
        this.result.add(
            Issue(
                this.sourceFilePath,
                name,
                message,
                ktElement.location(),
            )
        )
    }

}

private fun KtElement.location(): Location {
    val document = containingFile.viewProvider.document

    val startLineNumber = document.getLineNumber(textRange.startOffset)
    val startLineNumberOffset = document.getLineStartOffset(startLineNumber)
    val startLineOffset = textRange.startOffset - startLineNumberOffset
    val endLineNumber = document.getLineNumber(textRange.endOffset)
    val endLineNumberOffset = document.getLineStartOffset(endLineNumber)
    val endLineOffset = textRange.endOffset - endLineNumberOffset

    return Location(startLineNumber + 1, startLineOffset, endLineNumber + 1, endLineOffset)
}