package org.bibletranslationtools.maui.common.usecases.batch

import io.reactivex.Completable
import org.bibletranslationtools.maui.common.data.Batch
import org.bibletranslationtools.maui.common.persistence.IDirectoryProvider
import javax.inject.Inject

class DeleteBatch @Inject constructor(private val directoryProvider: IDirectoryProvider) {
    fun delete(batch: Batch): Completable {
        return Completable.fromCallable {
            directoryProvider.deleteCachedFiles(
                batch.media.value.map { it.file }
            )
            batch.file.delete()
        }
    }
}