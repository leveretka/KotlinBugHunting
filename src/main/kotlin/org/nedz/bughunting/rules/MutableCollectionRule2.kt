package org.nedz.bughunting.rules

import org.jetbrains.kotlin.cfg.containingDeclarationForPseudocode
import org.jetbrains.kotlin.psi.*
import org.nedz.bughunting.api.FileContext
import org.nedz.bughunting.api.Rule

/**
 * This rule detects mutable collections that can be replaced with immutable ones.
 * It uses a more sophisticated approach than MutableCollectionRule1 by checking
 * if any mutating operations are performed on the collection.
 */
class MutableCollectionRule2 : Rule("Unnecessary Mutable Collection") {

    // List of mutating methods for collections
    private val mutatingMethods = listOf(
        "add", "addAll", "remove", "removeAll", "removeAt", "clear", "set", "put", "putAll"
    )

    override fun visitProperty(property: KtProperty, data: FileContext) {
        // Only check properties with explicit type references
        val typeReference = property.typeReference ?: return

        // Get the property name
        val propertyName = property.name ?: return

        val typeText = typeReference.text

        // Check if the type is MutableList or MutableSet
        val replacementType = when {
            typeText.contains("MutableList") -> "List"
            typeText.contains("MutableSet") -> "Set"
            else -> return
        }

        // Check if the property is used in a mutating operation
        val contextText = property.containingDeclarationForPseudocode?.text ?: property.containingFile.text
        val hasMutatingOperation = mutatingMethods.any { method ->
            // Look for patterns like "propertyName.method("
            contextText.contains("$propertyName.$method(")
        }

        // Only flag the property if it's not used in a mutating operation
        if (!hasMutatingOperation) {
            data.addIssue("This mutable collection can be replaced with $replacementType", typeReference)
        }
    }
}
