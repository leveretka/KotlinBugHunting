package org.nedz.bughunting.rules

import org.jetbrains.kotlin.descriptors.VariableAccessorDescriptor
import org.jetbrains.kotlin.descriptors.VariableDescriptorWithAccessors
import org.jetbrains.kotlin.descriptors.accessors
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtImportList
import org.jetbrains.kotlin.psi.KtPackageDirective
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtPropertyDelegate
import org.jetbrains.kotlin.psi.KtSimpleNameExpression
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.util.getResolvedCall
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameOrNull
import org.jetbrains.kotlin.resolve.descriptorUtil.getImportableDescriptor
import org.jetbrains.kotlin.resolve.descriptorUtil.isCompanionObject
import org.nedz.bughunting.api.FileContext
import org.nedz.bughunting.api.Rule

private val DELEGATES_IMPORTED_NAMES = setOf("getValue", "setValue", "provideDelegate")

class UnusedImport6 : Rule("Unused import 6") {

    override fun visitKtFile(file: KtFile, ctx: FileContext) {

        val unresolvedImports = ctx.bindingContext.diagnostics.noSuppression()
            .filter { it.factory == Errors.UNRESOLVED_REFERENCE }
            .mapNotNull { it.psiElement.getParentOfType<KtImportDirective>(false) }

        val references = file
            .children.asSequence().filter { it !is KtPackageDirective && it !is KtImportList }
            .flatMap { it.collectDescendantsOfType<KtSimpleNameExpression>() }
            .mapNotNull { ctx.bindingContext[BindingContext.REFERENCE_TARGET, it]?.getImportableDescriptor() }
            .toList()

        val propertyDelegates = file.collectDescendantsOfType<KtPropertyDelegate>()

        val calls = file.collectDescendantsOfType<KtCallExpression>()


        file.importDirectives
            .filterNot { unresolvedImports.contains(it) }
            .filterNot { it.isUsedAsDelegate(propertyDelegates, ctx.bindingContext) }
            .filterNot { it.isUsedAsInvoke(calls, ctx.bindingContext) }
            .filter {
                references.none { descriptor ->
                    descriptor.fqNameOrNull() == it.importedFqName ||
                            (
                                    descriptor.isCompanionObject() &&
                                            it.importedFqName == descriptor.fqNameOrNull()?.parent()
                                    )
                }
            }
            .forEach { ctx.addIssue("This import is unused", it) }

    }

    private fun KtImportDirective.isUsedAsDelegate(
        propertyDelegates: Collection<KtPropertyDelegate>,
        bindingContext: BindingContext
    ): Boolean {
        fun getFqNameFromResolvedCall(variableAccessor: VariableAccessorDescriptor): FqName? =
            bindingContext[BindingContext.DELEGATED_PROPERTY_RESOLVED_CALL, variableAccessor]
                ?.resultingDescriptor
                ?.fqNameOrNull()

        fun getFqNamesFromAccessor(ktProperty: KtProperty): List<FqName>? =
            (bindingContext[BindingContext.DECLARATION_TO_DESCRIPTOR, ktProperty] as? VariableDescriptorWithAccessors)?.accessors?.mapNotNull(
                ::getFqNameFromResolvedCall
            )

        return if (bindingContext == BindingContext.EMPTY) {
            importedName?.asString() in DELEGATES_IMPORTED_NAMES
        } else {
            propertyDelegates.flatMap { propDelegate ->
                bindingContext[BindingContext.DELEGATE_EXPRESSION_TO_PROVIDE_DELEGATE_CALL, propDelegate.expression]
                    .getResolvedCall(bindingContext)
                    ?.resultingDescriptor
                    ?.fqNameOrNull()?.let { listOf(it) }
                    ?: (propDelegate.parent as? KtProperty)?.let(::getFqNamesFromAccessor) ?: emptyList()
            }.let { delegateImports ->
                importedFqName in delegateImports
            }
        }
    }

    private fun KtImportDirective.isUsedAsInvoke(calls: Collection<KtCallExpression>, bindingContext: BindingContext) =
        if (bindingContext == BindingContext.EMPTY) {
            importedName?.asString() == "invoke"
        } else {
            // Lazy because we don't want to do this operation unless we find at least one "invoke" import
            val callsFqn by lazy(LazyThreadSafetyMode.NONE) {
                calls.mapNotNull { it.getResolvedCall(bindingContext)?.resultingDescriptor?.fqNameOrNull() }
            }
            if (importedName?.asString() == "invoke") true
            else {
                importedFqName in callsFqn
            }
        }
}