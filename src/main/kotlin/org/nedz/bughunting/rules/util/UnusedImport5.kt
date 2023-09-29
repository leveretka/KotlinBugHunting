package org.nedz.bughunting.rules.util

import org.jetbrains.kotlin.descriptors.VariableAccessorDescriptor
import org.jetbrains.kotlin.descriptors.VariableDescriptorWithAccessors
import org.jetbrains.kotlin.descriptors.accessors
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.name.FqName
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

class UnusedImport5 : Rule("Unused import 5") {

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

        file.importDirectives
            .filterNot { unresolvedImports.contains(it) }
            .filterNot { it.isUsedAsDelegate(propertyDelegates, ctx.bindingContext) }
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

        return propertyDelegates.flatMap { propDelegate ->
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