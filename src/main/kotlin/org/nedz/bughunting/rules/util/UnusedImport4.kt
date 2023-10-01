package org.nedz.bughunting.rules.util

import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameOrNull
import org.jetbrains.kotlin.resolve.descriptorUtil.isCompanionObject
import org.nedz.bughunting.api.FileContext
import org.nedz.bughunting.api.Rule

class UnusedImport4: Rule("Unused import 4") {

    override fun visitKtFile(file: KtFile, ctx: FileContext) {

        val unresolvedImports = ctx.semanticModel.getUnresolvedRefs()
            .mapNotNull { it.psiElement.getParentOfType<KtImportDirective>(false) }

        val references = file
            .children.asSequence().filter { it !is KtPackageDirective && it !is KtImportList }
            .flatMap { it.collectDescendantsOfType<KtSimpleNameExpression>() }
            .mapNotNull { ctx.semanticModel.getKtSimpleNameDescriptor(it) }
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