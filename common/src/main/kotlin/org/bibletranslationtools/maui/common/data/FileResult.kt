package org.bibletranslationtools.maui.common.data

import java.io.File

class FileResult(
    val status: FileStatus,
    val data: Media?,
    val requestedFile: File? = null
)