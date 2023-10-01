package org.nedz.bughunting.rules.util

import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameOrNull
import org.jetbrains.kotlin.resolve.descriptorUtil.isCompanionObject
import org.nedz.bughunting.api.FileContext
import org.nedz.bughunting.api.Rule
import org.nedz.bughunting.api.SemanticModel

class UnusedImport6 : Rule("Unused import 6") {

    override fun visitKtFile(file: KtFile, ctx: FileContext) {

        val unresolvedImports = ctx.semanticModel.getUnresolvedRefs()
            .mapNotNull { it.psiElement.getParentOfType<KtImportDirective>(false) }

        val references = file
            .children.asSequence().filter { it !is KtPackageDirective && it !is KtImportList }
            .flatMap { it.collectDescendantsOfType<KtSimpleNameExpression>() }
            .mapNotNull { ctx.semanticModel.getKtSimpleNameDescriptor(it) }
            .toList()

        val propertyDelegates = file.collectDescendantsOfType<KtPropertyDelegate>()

        val calls by lazy(LazyThreadSafetyMode.NONE) {  file.collectDescendantsOfType<KtCallExpression>() }


        file.importDirectives
            .filterNot { unresolvedImports.contains(it) }
            .filterNot { it.isUsedAsDelegate(propertyDelegates, ctx.semanticModel) }
            .filterNot { it.isUsedAsInvoke(calls, ctx.semanticModel) }
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
        semanticModel: SemanticModel,
    ) = propertyDelegates.flatMap { propDelegate ->
        semanticModel.getPropDelegateQualifiedNames(propDelegate)
    }.let { delegateImports ->
        importedFqName in delegateImports
    }


    private fun KtImportDirective.isUsedAsInvoke(calls: Collection<KtCallExpression>, semanticModel: SemanticModel): Boolean {
        val callsFqn by lazy(LazyThreadSafetyMode.NONE) {
            calls.mapNotNull { semanticModel.getCallExpressionFqn(it) }
        }
        return importedName?.asString() == "invoke" && importedFqName in callsFqn
    }

}