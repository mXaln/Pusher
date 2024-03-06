package org.bibletranslationtools.maui.common.data

import org.bibletranslationtools.maui.common.extensions.CompressedExtensions
import org.bibletranslationtools.maui.common.extensions.ContainerExtensions
import org.bibletranslationtools.maui.common.extensions.MediaExtensions
import java.io.File

data class Media(
    val file: File,
    val language: String? = null,
    val resourceType: String? = null,
    val book: String? = null,
    val chapter: Int? = null,
    val mediaExtension: MediaExtension? = null,
    val mediaQuality: MediaQuality? = null,
    val grouping: Grouping? = null,
    val status: FileStatus? = null,
    val statusMessage: String? = null
) {
    val extension = MediaExtensions.of(file.extension)

    val isContainer = ContainerExtensions.isSupported(extension.norm)

    val isCompressed =
        !isContainer && CompressedExtensions.isSupported(extension.norm)

    val isContainerAndCompressed =
        isContainer && CompressedExtensions.isSupported(mediaExtension.toString())
}
