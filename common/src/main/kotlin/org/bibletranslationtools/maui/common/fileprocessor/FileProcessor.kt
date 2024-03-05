package org.bibletranslationtools.maui.common.fileprocessor

import org.bibletranslationtools.maui.common.data.Media
import org.bibletranslationtools.maui.common.data.FileResult
import org.bibletranslationtools.maui.common.data.FileStatus
import org.bibletranslationtools.maui.common.usecases.ParseFileName
import org.slf4j.LoggerFactory
import java.io.File
import java.util.Queue

abstract class FileProcessor {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    abstract fun process(
        file: File,
        fileQueue: Queue<File>,
        resultList: MutableList<FileResult>
    ): FileStatus

    protected fun getMedia(file: File): Media {
        return try {
            ParseFileName(file).parse()
        } catch (e: Exception) {
            logger.error("Error while parsing file name.", e)
            throw e
        }
    }
}