package org.bibletranslationtools.maui.jvm.di

import dagger.Component
import org.bibletranslationtools.maui.common.persistence.IDirectoryProvider
import org.bibletranslationtools.maui.jvm.MainApp
import org.bibletranslationtools.maui.jvm.di.modules.DirectoryProviderModule
import org.bibletranslationtools.maui.jvm.ui.batch.BatchViewModel
import javax.inject.Singleton

@Component(
    modules = [DirectoryProviderModule::class]
)

@Singleton
interface AppDependencyGraph {
    fun inject(app: MainApp)
    fun injectDirectoryProvider(): IDirectoryProvider
    fun inject(viewModel: BatchViewModel)
}