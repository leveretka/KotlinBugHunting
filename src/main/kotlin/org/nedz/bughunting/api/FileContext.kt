package org.nedz.bughunting.api

import org.jetbrains.kotlin.analysis.api.standalone.StandaloneAnalysisAPISession
import org.jetbrains.kotlin.descriptors.VariableAccessorDescriptor
import org.jetbrains.kotlin.descriptors.VariableDescriptorWithAccessors
import org.jetbrains.kotlin.descriptors.accessors
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtPropertyDelegate
import org.jetbrains.kotlin.psi.KtSimpleNameExpression
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.util.getResolvedCall
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameOrNull
import org.jetbrains.kotlin.resolve.descriptorUtil.getImportableDescriptor

data class FileContext(
    val sourceFilePath: String,
    val ktFile: KtFile,
    val semanticModel: SemanticModel,
    val result: MutableList<Issue>,
)

class SemanticModel(private val bindingContext: BindingContext, val session: StandaloneAnalysisAPISession? = null) {

    fun getCallExpressionFqn(call: KtCallExpression) =
        call.getResolvedCall(bindingContext)?.resultingDescriptor?.fqNameOrNull()

    fun getKtSimpleNameDescriptor(simpleExpr: KtSimpleNameExpression) =
        bindingContext[BindingContext.REFERENCE_TARGET, simpleExpr]?.getImportableDescriptor()

    fun getUnresolvedRefs() =
        bindingContext.diagnostics.noSuppression().filter { it.factory == Errors.UNRESOLVED_REFERENCE }

    fun getPropDelegateQualifiedNames(propDelegate: KtPropertyDelegate) =
        bindingContext[BindingContext.DELEGATE_EXPRESSION_TO_PROVIDE_DELEGATE_CALL, propDelegate.expression]
            .getResolvedCall(bindingContext)
            ?.resultingDescriptor
            ?.fqNameOrNull()?.let { listOf(it) }
            ?: (propDelegate.parent as? KtProperty)?.let(::getFqNamesFromAccessor) ?: emptyList()

    private fun getFqNameFromResolvedCall(variableAccessor: VariableAccessorDescriptor): FqName? =
        bindingContext[BindingContext.DELEGATED_PROPERTY_RESOLVED_CALL, variableAccessor]
            ?.resultingDescriptor
            ?.fqNameOrNull()

    private fun getFqNamesFromAccessor(ktProperty: KtProperty): List<FqName>? =
        (bindingContext[BindingContext.DECLARATION_TO_DESCRIPTOR, ktProperty] as? VariableDescriptorWithAccessors)?.accessors?.mapNotNull(
            ::getFqNameFromResolvedCall
        )

}
