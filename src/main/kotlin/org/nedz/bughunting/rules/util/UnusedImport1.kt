package org.nedz.bughunting.rules.util

import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtImportList
import org.jetbrains.kotlin.psi.KtPackageDirective
import org.jetbrains.kotlin.psi.KtSimpleNameExpression
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameOrNull
import org.nedz.bughunting.api.FileContext
import org.nedz.bughunting.api.Rule

class UnusedImport1: Rule("Unused import") {

    override fun visitKtFile(file: KtFile, ctx: FileContext) {

        val references = file
            .children.asSequence().filter { it !is KtPackageDirective && it !is KtImportList }
            .flatMap { it.collectDescendantsOfType<KtSimpleNameExpression>() }
            .mapNotNull { ctx.semanticModel.getKtSimpleNameDescriptor(it)?.fqNameOrNull() }
            .toList()

        file.importDirectives
            .filterNot { references.contains(it.importedFqName) }
            .forEach { ctx.addIssue("This import is unused", it) }

    }
}