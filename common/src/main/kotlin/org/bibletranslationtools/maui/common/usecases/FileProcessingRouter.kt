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
    /**
     * Queue of pairs where first item is the processed file and second item is its parent file
     * For example .orature file contains wav or mp3 files and all its files will have it as parent
     * If a single .wav file is being processed, then the parent will be null
     */
    private val fileQueue: Queue<Pair<File, File?>> = LinkedList()

    @Throws(IOException::class)
    fun handleFiles(files: List<File>): List<FileResult> {
        val resultList = mutableListOf<FileResult>()
        fileQueue.addAll(files.map { it to null })

        while (fileQueue.isNotEmpty()) {
            processFile(fileQueue.poll(), resultList)
        }

        return resultList
    }

    private fun processFile(file: Pair<File, File?>, resultList: MutableList<FileResult>) {
        processors.forEach {
            it.process(file.first, fileQueue, file.second)?.let { result ->
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