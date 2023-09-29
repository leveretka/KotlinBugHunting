package org.nedz.bughunting.rules

import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtImportList
import org.jetbrains.kotlin.psi.KtPackageDirective
import org.jetbrains.kotlin.psi.KtSimpleNameExpression
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameOrNull
import org.jetbrains.kotlin.resolve.descriptorUtil.getImportableDescriptor
import org.nedz.bughunting.api.FileContext
import org.nedz.bughunting.api.Rule

class UnusedImportRule : Rule("Unused Import"){

    override fun visitKtFile(file: KtFile, ctx: FileContext) {

        val references = file
            .children.asSequence().filter { it !is KtPackageDirective && it !is KtImportList }
            .flatMap { it.collectDescendantsOfType<KtSimpleNameExpression>() }
            .mapNotNull {
                ctx.bindingContext[BindingContext.REFERENCE_TARGET, it]?.getImportableDescriptor()?.fqNameOrNull()
            }
            .toList()

        file.importDirectives
            .filterNot { references.contains(it.importedFqName) }
            .forEach { ctx.addIssue("This import is unused", it) }

    }
}