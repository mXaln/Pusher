package org.bibletranslationtools.maui.common.data

import java.io.File
import java.util.Date

data class Batch(
    val file: File,
    val name: String,
    val created: Date,
    val media: Lazy<List<FileData>>
)