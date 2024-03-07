package org.bibletranslationtools.maui.common.usecases

import org.bibletranslationtools.maui.common.data.FileResult
import org.bibletranslationtools.maui.common.fileprocessor.*
import org.bibletranslationtools.maui.common.persistence.IDirectoryProvider
import java.io.File
import java.io.IOException
import java.util.Queue
import java.util.LinkedList
import javax.inject.Inject

class FileProcessingRouter @Inject constructor(private val directoryProvider: IDirectoryProvider) {
    private val processors: List<FileProcessor> = getProcessors()
    private val fileQueue: Queue<File> = LinkedList()

    @Throws(IOException::class)
    fun handleFiles(files: List<File>): List<FileResult> {
        val resultList = mutableListOf<FileResult>()
        fileQueue.addAll(files)

        while (fileQueue.isNotEmpty()) {
            processFile(fileQueue.poll(), resultList)
        }

        return resultList
    }

    private fun processFile(file: File, resultList: MutableList<FileResult>) {
        processors.forEach {
            it.process(file, fileQueue)?.let { result ->
                resultList.add(result)
            }
        }
    }

    private fun getProcessors(): List<FileProcessor> {
        return listOf(
            CueProcessor(),
            JpgProcessor(),
            Mp3Processor(),
            TrProcessor(directoryProvider),
            WavProcessor(),
            OratureFileProcessor(directoryProvider)
        )
    }
}