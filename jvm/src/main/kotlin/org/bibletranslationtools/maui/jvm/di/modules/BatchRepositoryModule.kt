package org.bibletranslationtools.maui.jvm.di.modules

import dagger.Binds
import dagger.Module
import org.bibletranslationtools.maui.common.persistence.IBatchRepository
import org.bibletranslationtools.maui.jvm.persistence.BatchRepository

@Module
abstract class BatchRepositoryModule {
    @Binds
    abstract fun inject(repository: BatchRepository) : IBatchRepository
}