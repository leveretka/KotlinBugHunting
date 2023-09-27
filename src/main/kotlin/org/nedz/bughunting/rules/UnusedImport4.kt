package org.nedz.bughunting.rules

import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtImportList
import org.jetbrains.kotlin.psi.KtPackageDirective
import org.jetbrains.kotlin.psi.KtSimpleNameExpression
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameOrNull
import org.jetbrains.kotlin.resolve.descriptorUtil.getImportableDescriptor
import org.jetbrains.kotlin.resolve.descriptorUtil.isCompanionObject
import org.nedz.bughunting.api.FileContext
import org.nedz.bughunting.api.Rule

class UnusedImport4: Rule("Unused import 4") {

    override fun visitKtFile(file: KtFile, ctx: FileContext) {

        val unresolvedImports = ctx.bindingContext.diagnostics.noSuppression()
            .filter { it.factory == Errors.UNRESOLVED_REFERENCE }
            .mapNotNull { it.psiElement.getParentOfType<KtImportDirective>(false) }

        val references = file
            .children.asSequence().filter { it !is  KtPackageDirective && it !is KtImportList }
            .flatMap { it.collectDescendantsOfType<KtSimpleNameExpression>() }
            .mapNotNull { ctx.bindingContext[BindingContext.REFERENCE_TARGET, it]?.getImportableDescriptor() }
            .toList()

        file.importDirectives
            .filterNot { unresolvedImports.contains(it) }
            .filter {
                references.none {
                    descriptor -> descriptor.fqNameOrNull() == it.importedFqName  ||
                        (
                                descriptor.isCompanionObject() &&
                                        it.importedFqName == descriptor.fqNameOrNull()?.parent()
                        )
                }
            }
            .forEach { ctx.addIssue("This import is unused", it) }

    }
}