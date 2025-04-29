package org.nedz.bughunting.engine

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile
import org.nedz.bughunting.api.FileContext
import org.nedz.bughunting.api.Issue
import org.nedz.bughunting.api.Rule
import org.nedz.bughunting.api.SemanticModel
import java.io.File

class Analyzer(private val rules: List<Rule>) {

    fun analyze(sources: List<File>, classpath: List<String>) : List<Issue> {
        val results = mutableListOf<Issue>()

        val env = Env(classpath)
        val virtualFileSystem = KotlinFileSystem()
        val k2Session = createK2AnalysisSession(
            parentDisposable = env.disposable,
            compilerConfiguration = env.configuration,
            virtualFiles = sources.map {
                KotlinVirtualFile(
                    virtualFileSystem,
                    it,
                    { it.readText() },
                )
            }
        )
        val sourceFiles: List<Pair<KtFile, String>> = sources.map { s -> k2Session.modulesWithFiles.values.first().find { it.virtualFile.path == s.absolutePath }  as KtFile to s.absolutePath }
        val semanticModel = SemanticModel()

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
