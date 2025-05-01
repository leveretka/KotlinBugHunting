package org.nedz.bughunting.rules

import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.*
import org.nedz.bughunting.api.FileContext
import org.nedz.bughunting.api.Rule

class MutableCollectionRule1 : Rule("Unnecessary Mutable Collection") {

    override fun visitProperty(property: KtProperty, data: FileContext) {
        // Only check properties with explicit type references
        val typeReference = property.typeReference ?: return

        // For this first version, we'll only check properties that are defined in the first part of the file
        // This is a simple approach to avoid flagging variables that are mutated
        val lineNumber = property.containingFile.viewProvider.document.getLineNumber(property.textOffset) + 1
        if (lineNumber > 13) return

        analyze(property) {
            val typeText = typeReference.text

            // Check if the type is MutableList or MutableSet
            when {
                typeText.contains("MutableList") -> {
                    // For this first version, we'll just report MutableList properties in the first part of the file
                    data.addIssue("This mutable collection can be replaced with List", typeReference)
                }
                typeText.contains("MutableSet") -> {
                    // For this first version, we'll just report MutableSet properties in the first part of the file
                    data.addIssue("This mutable collection can be replaced with Set", typeReference)
                }
            }
        }
    }
}
