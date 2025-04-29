package org.nedz.bughunting.api

import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.KaCallableMemberCall
import org.jetbrains.kotlin.analysis.api.resolution.successfulCallOrNull
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtFile

data class FileContext(
    val sourceFilePath: String,
    val ktFile: KtFile,
    val semanticModel: SemanticModel,
    val result: MutableList<Issue>,
)

class SemanticModel {

    fun getCallExpressionFqn(call: KtCallExpression) = analyze(call) {
        call.resolveToCall()?.successfulCallOrNull<KaCallableMemberCall<*,*>>()
            ?.partiallyAppliedSymbol?.signature?.callableId?.asSingleFqName()
    }
}
