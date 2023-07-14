package org.bibletranslationtools.maui.jvm.ui

import javafx.beans.binding.Bindings
import javafx.beans.binding.BooleanBinding
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import org.bibletranslationtools.maui.common.data.FileData
import org.bibletranslationtools.maui.common.data.Grouping
import org.bibletranslationtools.maui.common.data.MediaExtension
import org.bibletranslationtools.maui.common.data.MediaQuality
import org.bibletranslationtools.maui.common.extensions.CompressedExtensions
import tornadofx.*

data class FileDataItem(private val data: FileData) : Comparable<FileDataItem> {

    val file = data.file

    val languageProperty = SimpleStringProperty(data.language)
    var language: String? by languageProperty

    val resourceTypeProperty = SimpleStringProperty(data.resourceType)
    var resourceType: String? by resourceTypeProperty

    val bookProperty = SimpleStringProperty(data.book)
    var book: String? by bookProperty

    val chapterProperty = SimpleStringProperty(data.chapter?.toString())
    var chapter: String? by chapterProperty

    val mediaExtensionProperty = SimpleObjectProperty<MediaExtension>(data.mediaExtension)
    var mediaExtension: MediaExtension? by mediaExtensionProperty

    val mediaQualityProperty = SimpleObjectProperty<MediaQuality>(data.mediaQuality)
    var mediaQuality: MediaQuality? by mediaQualityProperty

    val groupingProperty = SimpleObjectProperty<Grouping>(data.grouping)
    var grouping: Grouping? by groupingProperty

    val isContainerProperty = SimpleBooleanProperty(data.isContainer)
    val isContainer by isContainerProperty

    val isCompressedProperty = SimpleBooleanProperty(data.isCompressed)
    val isCompressed by isCompressedProperty
    val isContainerAndCompressed: BooleanBinding = Bindings.createBooleanBinding(
        {
            mediaExtension?.let {
                isContainer && CompressedExtensions.isSupported(mediaExtension.toString())
            } ?: false
        },
        mediaExtensionProperty
    )

    val mediaExtensionAvailable = SimpleBooleanProperty(isContainer)
    val mediaQualityAvailable: BooleanBinding = Bindings.createBooleanBinding(
        {
            isContainerAndCompressed.value || isCompressed
        },
        mediaExtensionProperty
    )

    override fun compareTo(other: FileDataItem): Int {
        return FileDataItemComparator().compare(this, other)
    }
}
