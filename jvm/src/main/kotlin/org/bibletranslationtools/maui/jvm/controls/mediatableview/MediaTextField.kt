package org.bibletranslationtools.maui.jvm.controls.mediatableview

import com.jfoenix.controls.JFXTextField
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.Tooltip
import org.bibletranslationtools.maui.jvm.controls.mediafilter.MAX_CHAPTER_LENGTH
import tornadofx.*

class MediaTextField : JFXTextField() {

    val titleProperty = SimpleStringProperty()
    private val onTextChangedProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    init {
        addClass("media-text-field")

        promptTextProperty().bind(titleProperty)
        tooltipProperty().bind(titleProperty.objectBinding {
            it?.let { Tooltip(it).apply {
                Tooltip.install(this@MediaTextField, this)
            }}
        })

        filterInput {
            it.controlNewText.isInt() && it.controlNewText.length <= MAX_CHAPTER_LENGTH
        }

        textProperty().onChange {
            onTextChangedProperty.value?.handle(ActionEvent(it, this))
        }

        setOnAction {
            onTextChangedProperty.value?.handle(ActionEvent(text, this))
        }
    }

    fun setOnTextChanged(op: (String) -> Unit) {
        onTextChangedProperty.set(EventHandler {
            op.invoke(it.source as String)
        })
    }
}