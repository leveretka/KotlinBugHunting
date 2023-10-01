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

    }
}