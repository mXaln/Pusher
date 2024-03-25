package org.bibletranslationtools.maui.common.data

import java.io.File

class FileResult(
    val status: FileStatus,
    val statusMessage: String? = null,
    val data: Media? = null,
    val file: File? = null
)