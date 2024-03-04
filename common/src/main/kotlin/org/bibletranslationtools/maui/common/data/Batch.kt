package org.bibletranslationtools.maui.common.data

import java.io.File
import java.time.LocalDateTime

data class Batch(
    val file: File,
    val name: String,
    val created: LocalDateTime,
    val media: Lazy<List<FileData>>
)