package org.bibletranslationtools.maui.jvm.controls.filedatafilter

import io.reactivex.Observable
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.event.EventTarget
import javafx.scene.control.Control
import javafx.scene.control.Skin
import org.bibletranslationtools.maui.common.data.Grouping
import org.bibletranslationtools.maui.common.data.MediaExtension
import org.bibletranslationtools.maui.common.data.MediaQuality
import org.bibletranslationtools.maui.jvm.controls.skins.FileDataFilterSkin
import tornadofx.*

const val MAX_CHAPTER_LENGTH = 3

class FileDataFilter : Control() {

    val languageLabelProperty = SimpleStringProperty()
    val languagesProperty = SimpleListProperty<String>()
    val selectedLanguageProperty = SimpleObjectProperty<String?>()

    val resourceTypeLabelProperty = SimpleStringProperty()
    val resourceTypesProperty = SimpleListProperty<String>()
    val selectedResourceTypeProperty = SimpleObjectProperty<String?>()

    val bookLabelProperty = SimpleStringProperty()
    val booksProperty = SimpleListProperty<String>()
    val selectedBookProperty = SimpleObjectProperty<String?>()

    val chapterLabelProperty = SimpleStringProperty()
    val chapterProperty = SimpleObjectProperty<String?>()

    val mediaExtensionLabelProperty = SimpleStringProperty()
    val mediaExtensionsProperty = SimpleListProperty<MediaExtension>()
    val selectedMediaExtensionProperty = SimpleObjectProperty<MediaExtension?>()

    val mediaQualityLabelProperty = SimpleStringProperty()
    val mediaQualitiesProperty = SimpleListProperty<MediaQuality>()
    val selectedMediaQualityProperty = SimpleObjectProperty<MediaQuality?>()

    val groupingLabelProperty = SimpleStringProperty()
    val groupingsProperty = SimpleListProperty<Grouping>()
    val selectedGroupingProperty = SimpleObjectProperty<Grouping?>()

    val onConfirmActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    var callbackObserver: Observable<Boolean>? = null

    private val userAgentStyleSheet = javaClass.getResource("/css/file-data-filter.css")?.toExternalForm() ?: ""

    init {
        initialize()
    }

    override fun createDefaultSkin(): Skin<*> {
        return FileDataFilterSkin(this)
    }

    override fun getUserAgentStylesheet(): String {
        return userAgentStyleSheet
    }

    private fun initialize() {
        stylesheets.setAll(userAgentStyleSheet)
    }

    fun onConfirmAction(op: () -> Unit) {
        onConfirmActionProperty.set(EventHandler { op.invoke() })
    }

    fun onConfirmCallback(answer: Boolean) {
        callbackObserver = Observable.just(answer)
    }

    fun reset() {
        selectedLanguageProperty.set(null)
        selectedResourceTypeProperty.set(null)
        selectedBookProperty.set(null)
        chapterProperty.set(null)
        selectedMediaExtensionProperty.set(null)
        selectedMediaQualityProperty.set(null)
        selectedGroupingProperty.set(null)
    }
}

fun EventTarget.filedatafilter(op: FileDataFilter.() -> Unit = {}): FileDataFilter {
    val fileDataFilter = FileDataFilter()
    return opcr(this, fileDataFilter, op)
}
