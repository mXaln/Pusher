package org.bibletranslationtools.maui.jvm.controls.mediatableview

import com.jfoenix.controls.JFXComboBox
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableListValue
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.TableCell
import org.bibletranslationtools.maui.jvm.ui.MediaItem
import org.bibletranslationtools.maui.jvm.ui.mediacell.ErrorOccurredEvent
import tornadofx.FX
import tornadofx.addClass
import tornadofx.onChange
import tornadofx.selectedItem

class OptionTableCell<T>(
    options: ObservableListValue<T>,
    editable: Boolean = true
) : TableCell<MediaItem, T>() {

    private val onOptionChangedProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    private val oldItemValueProperty = SimpleObjectProperty<T>()
    private val optionsCombo = JFXComboBox(options).apply {
        addClass("options-combo-box")

        isEditable = editable

        itemProperty().onChange {
            it?.let(selectionModel::select)
            oldItemValueProperty.set(it)
        }

        setOnAction {
            if (selectedItem in items) {
                onOptionChangedProperty.value?.handle(ActionEvent(selectedItem, this))
            } else {
                if (selectedItem != null) {
                    selectionModel.select(oldItemValueProperty.value)
                    FX.eventbus.fire(ErrorOccurredEvent("Option $selectedItem not found in the list."))
                }
            }
        }
    }

    init {
        addClass("options-cell")
    }

    override fun updateItem(item: T?, empty: Boolean) {
        super.updateItem(item, empty)

        if (empty) {
            graphic = null
            return
        }

        graphic = optionsCombo
    }

    @Suppress("UNCHECKED_CAST")
    fun setOnOptionChanged(op: (T) -> Unit) {
        onOptionChangedProperty.set(EventHandler {
            op.invoke(it.source as T)
        })
    }
}