package org.bibletranslationtools.maui.common.usecases.batch

import io.mockk.every
import io.mockk.mockk
import org.bibletranslationtools.maui.common.data.Batch
import org.bibletranslationtools.maui.common.persistence.IBatchRepository
import org.bibletranslationtools.maui.common.persistence.IDirectoryProvider
import org.junit.Assert
import org.junit.Test

class DeleteBatchTest {
    private val directoryProvider = mockk<IDirectoryProvider> {
        every { deleteCachedFiles(any()) } returns Unit
        every { batchDirectory } returns kotlin.io.path.createTempDirectory("batches").toFile()
    }
    private val batchRepository = mockk<IBatchRepository> {
        every { deleteBatch(any()) } returns Unit
    }

    @Test
    fun deleteBatchSuccess() {
        val batchFile = directoryProvider.batchDirectory.resolve("batch.maui").apply {
            createNewFile()
            deleteOnExit()
        }
        val batch = Batch(
            batchFile,
            "Batch",
            "2024-01-01T01-01-01",
            lazy { listOf() }
        )

        Assert.assertEquals(true, batchFile.exists())

        val result = DeleteBatch(batchRepository).delete(batch).test()

        result.assertComplete()
        result.assertNoErrors()
    }
}