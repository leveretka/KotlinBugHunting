package org.nedz.bughunting.api

import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.BindingContext

data class FileContext(
    val sourceFilePath: String,
    val ktFile: KtFile,
    val bindingContext: BindingContext,
    val result: MutableList<Issue>,
)