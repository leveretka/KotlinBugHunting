package org.nedz.bughunting.rules

import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.utils.addToStdlib.ifFalse
import org.nedz.bughunting.api.FileContext
import org.nedz.bughunting.api.Rule

private val nameRegex = Regex("^[a-z][a-zA-Z0-9]*$")

class BadNameFunctionNameRule2 : Rule("BadNameFunctionName") {

    override fun visitNamedFunction(function: KtNamedFunction, data: FileContext) {
        val functionName = function.name
        functionName?.matches(nameRegex)?.ifFalse {
            data.addIssue(
                "This is a bad name",
                function.nameIdentifier!!
            )
        }
    }

}
