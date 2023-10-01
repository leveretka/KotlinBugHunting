package org.nedz.bughunting.rules

import org.jetbrains.kotlin.psi.KtFile
import org.nedz.bughunting.api.FileContext
import org.nedz.bughunting.api.Rule

class UnusedImportRule : Rule("Unused Import"){

    override fun visitKtFile(file: KtFile, ctx: FileContext) {

    }
}