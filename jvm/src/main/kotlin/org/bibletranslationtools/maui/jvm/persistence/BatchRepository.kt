package org.bibletranslationtools.maui.jvm.persistence

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.bibletranslationtools.maui.common.MauiInfo
import org.bibletranslationtools.maui.common.data.Batch
import org.bibletranslationtools.maui.common.persistence.IBatchRepository
import org.bibletranslationtools.maui.common.persistence.IDirectoryProvider
import javax.inject.Inject

class BatchRepository @Inject constructor(private val directoryProvider: IDirectoryProvider) : IBatchRepository {
    private val batchMapper = ObjectMapper().registerKotlinModule()

    override fun createBatch(batch: Batch) {
        if (!batch.file.exists()) {
            batch.file.createNewFile()
        }
        saveBatch(batch)
    }

    override fun saveBatch(batch: Batch) {
        val jsonStr = batchMapper.writeValueAsString(batch)
        batch.file.writeText(jsonStr)
    }

    override fun getAll(): List<Batch> {
        return directoryProvider.batchDirectory.listFiles()
            ?.filter { it.isFile && it.extension == MauiInfo.EXTENSION }
            ?.map { file ->
                val json = file.readText()
                batchMapper.readValue(json)
            } ?: listOf()
    }
}
