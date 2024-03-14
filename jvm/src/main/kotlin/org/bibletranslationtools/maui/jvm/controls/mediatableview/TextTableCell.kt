package org.bibletranslationtools.maui.jvm.controls.mediatableview

import com.jfoenix.controls.JFXTextField
import javafx.beans.property.SimpleObjectProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.TableCell
import org.bibletranslationtools.maui.jvm.controls.mediafilter.MAX_CHAPTER_LENGTH
import org.bibletranslationtools.maui.jvm.ui.MediaItem
import tornadofx.addClass
import tornadofx.filterInput
import tornadofx.isInt
import tornadofx.onChange

class TextTableCell : TableCell<MediaItem, String>() {

    private val onTextChangedProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    private val textEdit = JFXTextField().apply {
        filterInput {
            it.controlNewText.isInt() && it.controlNewText.length <= MAX_CHAPTER_LENGTH
        }

        itemProperty().onChange {
            it?.let { text = it }
        }

        textProperty().onChange {
            onTextChangedProperty.value?.handle(ActionEvent(it, this))
        }

        setOnAction {
            onTextChangedProperty.value?.handle(ActionEvent(text, this))
        }
    }

    init {
        addClass("text-cell")
    }

    override fun updateItem(item: String?, empty: Boolean) {
        super.updateItem(item, empty)

        if (empty) {
            graphic = null
            return
        }

        graphic = textEdit.apply {
            positionCaret(text.length)
        }
    }

    fun setOnTextChanged(op: (String) -> Unit) {
        onTextChangedProperty.set(EventHandler {
            op.invoke(it.source as String)
        })
    }
}