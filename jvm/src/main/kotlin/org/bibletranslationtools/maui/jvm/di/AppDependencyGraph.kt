package org.bibletranslationtools.maui.jvm.di

import dagger.Component
import org.bibletranslationtools.maui.jvm.MainApp
import org.bibletranslationtools.maui.jvm.di.modules.DirectoryProviderModule
import org.bibletranslationtools.maui.jvm.di.modules.ReadersModule
import org.bibletranslationtools.maui.jvm.di.modules.RepositoryModule
import org.bibletranslationtools.maui.jvm.ui.BatchDataStore
import org.bibletranslationtools.maui.jvm.ui.ImportFilesViewModel
import org.bibletranslationtools.maui.jvm.ui.batch.BatchViewModel
import org.bibletranslationtools.maui.jvm.ui.upload.UploadMediaViewModel
import javax.inject.Singleton

@Component(
    modules = [
        DirectoryProviderModule::class,
        RepositoryModule::class,
        ReadersModule::class
    ]
)

@Singleton
interface AppDependencyGraph {
    fun inject(app: MainApp)
    fun inject(viewModel: BatchViewModel)
    fun inject(viewModel: UploadMediaViewModel)
    fun inject(viewModel: ImportFilesViewModel)
    fun inject(dataStore: BatchDataStore)
}