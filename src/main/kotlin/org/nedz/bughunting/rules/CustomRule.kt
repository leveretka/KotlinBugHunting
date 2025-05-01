package org.nedz.bughunting.rules

import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.nedz.bughunting.api.FileContext
import org.nedz.bughunting.api.Rule

class CustomRule : Rule("Custom"){

    override fun visitNamedFunction(function: KtNamedFunction, data: FileContext) {
        analyze(function) {
            if (function.returnType.isShortType) {
                data.addIssue("Wrong return type", function.typeReference ?: function.nameIdentifier ?: function.funKeyword!!)
            }
        }
    }

}
