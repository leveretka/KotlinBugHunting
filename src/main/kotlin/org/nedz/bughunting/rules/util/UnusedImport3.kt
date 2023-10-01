package org.nedz.bughunting.rules.util

import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtImportList
import org.jetbrains.kotlin.psi.KtPackageDirective
import org.jetbrains.kotlin.psi.KtSimpleNameExpression
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameOrNull
import org.jetbrains.kotlin.resolve.descriptorUtil.isCompanionObject
import org.nedz.bughunting.api.FileContext
import org.nedz.bughunting.api.Rule

class UnusedImport3: Rule("Unused import 3") {

    override fun visitKtFile(file: KtFile, ctx: FileContext) {

        val references = file
            .children.asSequence().filter { it !is KtPackageDirective && it !is KtImportList }
            .flatMap { it.collectDescendantsOfType<KtSimpleNameExpression>() }
            .mapNotNull { ctx.semanticModel.getKtSimpleNameDescriptor(it) }
            .toList()

        file.importDirectives
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