package org.bibletranslationtools.maui.jvm.controls.mediatableview

import javafx.beans.property.SimpleObjectProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.TableCell
import org.bibletranslationtools.maui.jvm.data.MediaItem
import tornadofx.onChange

class TextCell : TableCell<MediaItem, String>() {

    private val onTextChangedProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    private val mediaTextField = MediaTextField().apply {
        itemProperty().onChange {
            it?.let { text = it }
        }

        setOnTextChanged {
            onTextChangedProperty.value?.handle(ActionEvent(it, this))
        }
    }

    override fun updateItem(item: String?, empty: Boolean) {
        super.updateItem(item, empty)

        if (empty) {
            graphic = null
            return
        }

        graphic = mediaTextField.apply {
            positionCaret(text.length)
        }
    }

    fun setOnTextChanged(op: (String) -> Unit) {
        onTextChangedProperty.set(EventHandler {
            op.invoke(it.source as String)
        })
    }
}