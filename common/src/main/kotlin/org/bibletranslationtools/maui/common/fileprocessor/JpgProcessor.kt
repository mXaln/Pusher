package org.bibletranslationtools.maui.common.fileprocessor

import org.bibletranslationtools.maui.common.data.FileResult
import org.bibletranslationtools.maui.common.data.FileStatus
import org.bibletranslationtools.maui.common.data.Media
import org.bibletranslationtools.maui.common.extensions.MediaExtensions
import org.bibletranslationtools.maui.common.validators.JpgValidator
import java.io.File
import java.lang.IllegalArgumentException
import java.util.Queue

class JpgProcessor : FileProcessor() {
    override fun process(
        file: File,
        fileQueue: Queue<File>,
        resultList: MutableList<FileResult>
    ): FileResult? {
        val ext = try {
            MediaExtensions.of(file.extension)
        } catch (ex: IllegalArgumentException) {
            null
        }

        if (ext != MediaExtensions.JPG) {
            return null
        }

        val media = try {
            JpgValidator(file).validate()
            getMedia(file)
        } catch (ex: Exception) {
            Media(
                file = file,
                status = FileStatus.WARNING,
                statusMessage = ex.message
            )
        }

        return FileResult(media.status!!, media.statusMessage, media)
    }
}