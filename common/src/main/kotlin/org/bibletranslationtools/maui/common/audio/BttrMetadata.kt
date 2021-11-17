package org.bibletranslationtools.maui.common.audio

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import org.wycliffeassociates.otter.common.audio.AudioCue

data class BttrMetadata(
    var anthology: String = "",
    var language: String = "",
    var version: String = "",
    @JsonAlias("book")
    var slug: String = "",
    @JsonProperty("book_number")
    var bookNumber: String = "",
    var mode: String = "",
    var chapter: String = "",
    var startv: String = "",
    var endv: String = "",
    var contributor: String = "",
    @JsonDeserialize(using = MarkerListDeserializer::class)
    @JsonSerialize(using = MarkerListSerializer::class)
    var markers: MutableList<AudioCue> = mutableListOf()
)
