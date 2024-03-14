package org.bibletranslationtools.maui.common.fileprocessor

import org.bibletranslationtools.maui.common.data.FileResult
import org.bibletranslationtools.maui.common.data.FileStatus
import org.bibletranslationtools.maui.common.data.Media
import org.bibletranslationtools.maui.common.extensions.MediaExtensions
import org.bibletranslationtools.maui.common.persistence.IDirectoryProvider
import org.bibletranslationtools.maui.common.validators.TrValidator
import java.io.File
import java.lang.IllegalArgumentException
import java.util.Queue

class TrProcessor(private val directoryProvider: IDirectoryProvider) : FileProcessor() {
    override fun process(
        file: File,
        fileQueue: Queue<Pair<File, File?>>,
        parentFile: File?
    ): FileResult? {
        val ext = try {
            MediaExtensions.of(file.extension)
        } catch (ex: IllegalArgumentException) {
            null
        }

        if (ext != MediaExtensions.TR) {
            return null
        }

        val media = try {
            TrValidator(directoryProvider, file).validate()
            getMedia(file, parentFile)
        } catch (ex: Exception) {
            Media(
                file = file,
                status = FileStatus.REJECTED,
                statusMessage = ex.message,
                parentFile = parentFile
            )
        }

        return FileResult(media.status!!, media.statusMessage, media)
    }
}