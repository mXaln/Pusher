package org.bibletranslationtools.maui.common.data

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import org.bibletranslationtools.maui.common.MediaDeserializer
import org.bibletranslationtools.maui.common.MediaSerializer
import java.io.File

data class Batch(
    val file: File,
    val name: String,
    val created: String,
    @JsonSerialize(using = MediaSerializer::class)
    @JsonDeserialize(using = MediaDeserializer::class)
    val media: Lazy<List<Media>>
)