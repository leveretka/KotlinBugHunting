package org.nedz.bughunting.engine

import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.environment.setIdeaIoUseFallback
import org.jetbrains.kotlin.cli.common.messages.AnalyzerWithCompilerReport
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.jvm.compiler.NoScopeRecordCliBindingTrace
import org.jetbrains.kotlin.cli.jvm.compiler.TopDownAnalyzerFacadeForJVM
import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoots
import com.intellij.openapi.Disposable
import com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.config.ApiVersion
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.JVMConfigurationKeys
import org.jetbrains.kotlin.config.LanguageVersion
import org.jetbrains.kotlin.config.LanguageVersionSettingsImpl
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.lazy.declarations.FileBasedDeclarationProviderFactory
import java.io.File

class Env(classpath: List<String>) {
    val disposable = Disposer.newDisposable()
    val configuration = compilerConfiguration(classpath)
    val kotlinCoreEnvironment = kotlinCoreEnvironment(configuration, disposable)
    val ktPsiFactory: KtPsiFactory = KtPsiFactory(kotlinCoreEnvironment.project, false)
}

fun kotlinCoreEnvironment(
    configuration: CompilerConfiguration,
    disposable: Disposable,
): KotlinCoreEnvironment {
    setIdeaIoUseFallback()
    configuration.put(
        CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY,
        MessageCollector.NONE
    )
    configuration.put(CommonConfigurationKeys.MODULE_NAME, "module")

    return KotlinCoreEnvironment.createForProduction(
        disposable,
        configuration,
        EnvironmentConfigFiles.JVM_CONFIG_FILES,
    )
}

fun bindingContext(
    env: KotlinCoreEnvironment,
    ktFiles: List<KtFile>,
): BindingContext {
    val analyzer = AnalyzerWithCompilerReport(
        MessageCollector.NONE,
        env.configuration.languageVersionSettings,
        false
    )
    analyzer.analyzeAndReport(ktFiles) {
        TopDownAnalyzerFacadeForJVM.analyzeFilesWithJavaIntegration(
            env.project,
            ktFiles,
            NoScopeRecordCliBindingTrace(env.project),
            env.configuration,
            env::createPackagePartProvider,
            ::FileBasedDeclarationProviderFactory
        )
    }
    return analyzer.analysisResult.bindingContext
}

fun compilerConfiguration(
    classpath: List<String>,
): CompilerConfiguration {
    val classpathFiles = classpath.map(::File)
    val versionSettings = LanguageVersionSettingsImpl(
        LanguageVersion.LATEST_STABLE,
        ApiVersion.createByLanguageVersion(LanguageVersion.LATEST_STABLE),
    )

    return CompilerConfiguration().apply {
        put(CommonConfigurationKeys.LANGUAGE_VERSION_SETTINGS, versionSettings)
        //put(JVMConfigurationKeys.JDK_RELEASE, 11)
        put(JVMConfigurationKeys.JDK_RELEASE, 21)
        put(JVMConfigurationKeys.JDK_HOME, File(System.getProperty("java.home")))
        addJvmClasspathRoots(classpathFiles)
    }
}

