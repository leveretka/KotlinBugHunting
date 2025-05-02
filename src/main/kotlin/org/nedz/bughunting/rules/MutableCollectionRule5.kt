package org.nedz.bughunting.rules

import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.*
import org.nedz.bughunting.api.FileContext
import org.nedz.bughunting.api.Rule
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.types.symbol
import org.jetbrains.kotlin.cfg.containingDeclarationForPseudocode
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.findDescendantOfType

/**
 * This rule detects mutable collections that can be replaced with immutable ones.
 * It extends MutableCollectionRule2 by also checking if the collection is passed to a function
 * that expects a mutable collection type, which would indicate that the collection might be mutated.
 */
class MutableCollectionRule5 : Rule("Unnecessary Mutable Collection") {

    // List of mutating methods for collections
    private val mutatingMethods = listOf(
        "add", "addAll", "remove", "removeAll", "removeAt", "clear", "set", "put", "putAll"
    )

    private val imMutableCollections =
        setOf(
            "kotlin.collections.Iterable",
            "kotlin.collections.List",
            "kotlin.collections.Set",
            "kotlin.collections.Collection"
        )

    override fun visitProperty(property: KtProperty, data: FileContext) {
        // Get the property name
        val propertyName = property.name ?: return

        val typeText = analyze(property) {
            property.symbol.returnType.symbol?.classId?.shortClassName?.asString() ?: return
        }

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
        } || checkIfPassedToMutableFunction(property, propertyName)
        || checkIfPassedToMutatingOperator(property, propertyName)

        // Only flag the property if it's not used in a mutating operation
        if (!hasMutatingOperation) {
            data.addIssue("This mutable collection can be replaced with $replacementType",
                property.typeReference ?: property.initializer ?: property.nameIdentifier ?: property)
        }
    }

    private fun checkIfPassedToMutableFunction(property: KtProperty, propertyName: String): Boolean = analyze(property) {
        val contextElement = property.containingDeclarationForPseudocode ?: property.containingFile

        contextElement.collectDescendantsOfType<KtCallExpression> { call ->
            val functionCall = call.resolveToCall()?.successfulFunctionCallOrNull()
            val parameterIndex = call.valueArgumentList?.arguments?.indexOf(call.valueArgumentList
                ?.findDescendantOfType<KtValueArgument> { it.text == propertyName}) ?: -1
            if (parameterIndex < 0) {
                false
            } else {
                val fqNameString = functionCall
                    ?.argumentMapping
                    ?.values?.withIndex()?.first { it.index == parameterIndex }?.value
                    ?.returnType?.symbol?.classId?.asSingleFqName()?.asString()
                fqNameString !in imMutableCollections
            }
        }.isNotEmpty()
    }

    private fun checkIfPassedToMutatingOperator(property: KtProperty, propertyName: String): Boolean = analyze(property) {
        val contextElement = property.containingDeclarationForPseudocode ?: property.containingFile
        contextElement.collectDescendantsOfType<KtBinaryExpression> { binaryExpression ->
            if (binaryExpression.operationToken != KtTokens.PLUSEQ && binaryExpression.operationToken != KtTokens.MINUSEQ)
                return@collectDescendantsOfType false
            val left = binaryExpression.left as? KtNameReferenceExpression ?: return@collectDescendantsOfType false
            return@collectDescendantsOfType left.text == propertyName
        }.isNotEmpty()
    }
}
