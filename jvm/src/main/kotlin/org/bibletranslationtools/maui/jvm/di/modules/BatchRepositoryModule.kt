package org.bibletranslationtools.maui.jvm.di.modules

import dagger.Binds
import dagger.Module
import org.bibletranslationtools.maui.common.persistence.IBatchRepository
import org.bibletranslationtools.maui.jvm.persistence.BatchRepository
import javax.inject.Singleton

@Module
abstract class BatchRepositoryModule {
    @Binds
    @Singleton
    abstract fun inject(repository: BatchRepository) : IBatchRepository
}