package org.nedz.bughunting.engine

import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtElement
import org.nedz.bughunting.api.FileContext
import org.nedz.bughunting.api.Issue
import org.nedz.bughunting.api.Rule
import org.nedz.bughunting.api.SemanticModel
import java.io.File

class Analyzer(private val rules: List<Rule>) {

    fun analyze(sources: List<File>, classpath: List<String>) : List<Issue> {
        val results = mutableListOf<Issue>()

        val env = Env(classpath)
        val sourceFiles = sources.map { env.ktPsiFactory.createFile(it.absolutePath, it.readText()) to it.absolutePath}

        val semanticModel = SemanticModel(bindingContext(env.kotlinCoreEnvironment, sourceFiles.map { it.first }))

        sourceFiles.forEach { file ->
            rules.forEach { rule ->
                flattenNodes(listOf(file.first)).forEach {
                    when (it) {
                        is KtElement -> it.accept(rule, FileContext(file.second, file.first, semanticModel, results))
                    }
                }
            }
        }
        return results

    }


}

private tailrec fun flattenNodes(childNodes: List<PsiElement>, acc: MutableList<PsiElement> = mutableListOf()): List<PsiElement> =
    if (childNodes.none()) acc
    else flattenNodes(childNodes = childNodes.flatMap { it.children.asList() }, acc = acc.apply { addAll(childNodes) })