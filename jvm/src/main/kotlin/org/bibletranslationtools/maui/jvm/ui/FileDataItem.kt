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

data class FileDataItem(private val data: FileData): Comparable<FileDataItem> {

    val file = data.file

    val initLanguage = data.language
    val languageProperty = SimpleStringProperty(initLanguage)
    var language: String? by languageProperty

    val initResourceType = data.resourceType
    val resourceTypeProperty = SimpleStringProperty(initResourceType)
    var resourceType: String? by resourceTypeProperty

    val initBook = data.book
    val bookProperty = SimpleStringProperty(initBook)
    var book: String? by bookProperty

    val initChapter = data.chapter?.toString()
    val chapterProperty = SimpleStringProperty(initChapter)
    var chapter: String? by chapterProperty

    val initMediaExtension = data.mediaExtension
    val mediaExtensionProperty = SimpleObjectProperty<MediaExtension>(initMediaExtension)
    var mediaExtension: MediaExtension? by mediaExtensionProperty

    val initMediaQuality = data.mediaQuality
    val mediaQualityProperty = SimpleObjectProperty<MediaQuality>(initMediaQuality)
    var mediaQuality: MediaQuality? by mediaQualityProperty

    val initGrouping = data.grouping
    val groupingProperty = SimpleObjectProperty<Grouping>(initGrouping)
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
