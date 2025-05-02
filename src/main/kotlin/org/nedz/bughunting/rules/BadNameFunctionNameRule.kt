package org.nedz.bughunting.rules

import org.jetbrains.kotlin.psi.KtNamedFunction
import org.nedz.bughunting.api.FileContext
import org.nedz.bughunting.api.Rule

val nameRegex = Regex("^[a-z][a-zA-Z0-9]*$")

class BadNameFunctionNameRule : Rule("BadNameFunctionName") {

    override fun visitNamedFunction(function: KtNamedFunction, data: FileContext) {
        val functionName = function.name!!
        if(!functionName.matches(nameRegex)) {
            data.addIssue(
                "This is a bad name",
                function.nameIdentifier!!
            )
        }
    }

}
