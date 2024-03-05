package org.bibletranslationtools.maui.common.fileprocessor

import org.bibletranslationtools.maui.common.data.FileResult
import org.bibletranslationtools.maui.common.data.FileStatus
import org.bibletranslationtools.maui.common.extensions.MediaExtensions
import org.bibletranslationtools.maui.common.validators.JpgValidator
import java.io.File
import java.lang.IllegalArgumentException
import java.util.Queue

class JpgProcessor: FileProcessor() {
    override fun process(
            file: File,
            fileQueue: Queue<File>,
            resultList: MutableList<FileResult>
    ): FileStatus {
        val ext = try {
            MediaExtensions.of(file.extension)
        } catch (ex: IllegalArgumentException) {
            null
        }

        if (ext != MediaExtensions.JPG) {
            return FileStatus.REJECTED
        }

        return try {
            JpgValidator(file).validate()
            val media = getMedia(file)
            val result = FileResult(status = FileStatus.PROCESSED, data = media)
            resultList.add(result)

            FileStatus.PROCESSED
        } catch (ex: Exception) {
            FileStatus.REJECTED
        }
    }
}