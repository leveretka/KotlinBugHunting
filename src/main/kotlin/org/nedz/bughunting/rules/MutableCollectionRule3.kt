package org.nedz.bughunting.rules

import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.*
import org.nedz.bughunting.api.FileContext
import org.nedz.bughunting.api.Rule
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.analysis.api.resolution.KaCallableMemberCall
import org.jetbrains.kotlin.analysis.api.resolution.successfulCallOrNull

/**
 * This rule detects mutable collections that can be replaced with immutable ones.
 * It extends MutableCollectionRule2 by also checking if the collection is passed to a function
 * that expects a mutable collection type, which would indicate that the collection might be mutated.
 */
class MutableCollectionRule3 : Rule("Unnecessary Mutable Collection") {

    // List of mutating methods for collections
    private val mutatingMethods = listOf(
        "add", "addAll", "remove", "removeAll", "removeAt", "clear", "set", "put", "putAll"
    )

    override fun visitProperty(property: KtProperty, data: FileContext) {
        // Only check properties with explicit type references
        val typeReference = property.typeReference ?: return

        // Get the property name
        val propertyName = property.name ?: return

        analyze(property) {
            val typeText = typeReference.text

            // Check if the type is MutableList or MutableSet
            val replacementType = when {
                typeText.contains("MutableList") -> "List"
                typeText.contains("MutableSet") -> "Set"
                else -> return@analyze
            }

            // Check if the property is used in a mutating operation
            val fileText = property.containingFile.text
            val hasMutatingOperation = mutatingMethods.any { method ->
                // Look for patterns like "propertyName.method("
                fileText.contains("$propertyName.$method(")
            }

            // If there's no direct mutating operation, check if it's passed to a function
            // that expects a mutable collection
            if (!hasMutatingOperation) {
                val isPassedToMutableFunction = checkIfPassedToMutableFunction(property, propertyName, data)

                // Only flag the property if it's not used in a mutating operation and not passed to a function
                // that expects a mutable collection
                if (!isPassedToMutableFunction) {
                    data.addIssue("This mutable collection can be replaced with $replacementType", typeReference)
                }
            }
        }
    }

    private fun checkIfPassedToMutableFunction(property: KtProperty, propertyName: String, data: FileContext): Boolean {
        // Get the file text
        val fileText = property.containingFile.text

        // Find all function calls in the file that might be passing this property
        val functionCallPattern = "$propertyName\\s*[,)]".toRegex()
        val functionCalls = functionCallPattern.findAll(fileText)

        // For each potential function call, check if the function parameter is a mutable collection
        return functionCalls.any { matchResult ->
            // Find the function call expression that contains this argument
            val offset = matchResult.range.first
            val element = property.containingFile.findElementAt(offset) ?: return@any false

            // Find the closest call expression
            var current: PsiElement? = element
            var callExpression: KtCallExpression? = null

            while (current != null && callExpression == null) {
                if (current is KtCallExpression) {
                    callExpression = current
                }
                current = current.parent
            }

            if (callExpression != null) {
                // Check if the function being called is likely to mutate the collection
                // For simplicity, we'll check if the function name is "mutate" or if the function signature contains "Mutable"
                val functionName = callExpression.calleeExpression?.text
                if (functionName == "mutate") {
                    // If the function is named "mutate", assume it mutates the collection
                    true
                } else {
                    // Otherwise, try to resolve the call and check its signature
                    analyze(callExpression) {
                        val resolvedCall = callExpression.resolveToCall()?.successfulCallOrNull<KaCallableMemberCall<*, *>>()
                        if (resolvedCall != null) {
                            // Check if the function signature contains "Mutable"
                            val signature = resolvedCall.toString()
                            signature.contains("Mutable")
                        } else {
                            false
                        }
                    } ?: false
                }
            } else {
                false
            }
        }
    }
}
