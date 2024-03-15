package org.bibletranslationtools.maui.jvm.controls.mediatableview

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableListValue
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.TableCell
import org.bibletranslationtools.maui.jvm.data.MediaItem
import tornadofx.onChange

class MediaComboBoxCell<T>(
    options: ObservableListValue<T>,
    editable: Boolean = true
) : TableCell<MediaItem, T>() {

    val titleProperty = SimpleStringProperty()
    private val selectedItemProperty = SimpleObjectProperty<T>()
    private val onOptionChangedProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    private val mediaComboBox = MediaComboBox(options, editable, false).apply {
        titleProperty.bind(this@MediaComboBoxCell.titleProperty)

        itemProperty().onChange {
            it?.let(selectionModel::select)
            selectedItemProperty.set(it)
        }

        setOnOptionChanged {
            onOptionChangedProperty.value?.handle(ActionEvent(it, this))
        }

        setOnError {
            selectionModel.select(selectedItemProperty.value)
        }
    }

    override fun updateItem(item: T?, empty: Boolean) {
        super.updateItem(item, empty)

        if (empty) {
            graphic = null
            return
        }

        graphic = mediaComboBox
    }

    @Suppress("UNCHECKED_CAST")
    fun setOnOptionChanged(op: (T) -> Unit) {
        onOptionChangedProperty.set(EventHandler {
            op.invoke(it.source as T)
        })
    }
}