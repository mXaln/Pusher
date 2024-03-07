package org.bibletranslationtools.maui.jvm.controls.dialog

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.Parent
import javafx.scene.layout.Priority
import org.bibletranslationtools.maui.jvm.customizeScrollbarSkin
import org.bibletranslationtools.maui.jvm.onChangeAndDoNow
import org.bibletranslationtools.maui.jvm.ui.UploadTarget
import org.kordamp.ikonli.javafx.FontIcon
import tornadofx.*

class ConfirmDialog : OtterDialog() {

    val titleIconProperty = SimpleObjectProperty<FontIcon>()
    val titleTextProperty = SimpleStringProperty()
    val messageTextProperty = SimpleStringProperty()
    val detailsTextProperty = SimpleStringProperty()
    val confirmButtonTextProperty = SimpleStringProperty()
    val confirmButtonIconProperty = SimpleObjectProperty<FontIcon>()
    val cancelButtonTextProperty = SimpleStringProperty()
    val cancelButtonIconProperty = SimpleObjectProperty<FontIcon>()
    val uploadTargetProperty = SimpleObjectProperty<UploadTarget>()

    private val onCancelActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    private val onConfirmActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    private lateinit var scroll: Parent

    private val content = vbox {
        addClass("confirm-dialog")

        uploadTargetProperty.onChangeAndDoNow {
            togglePseudoClass("accent", it == UploadTarget.DEV)
        }

        hbox {
            addClass("confirm-dialog__header")

            label {
                graphicProperty().bind(titleIconProperty)
            }

            label(titleTextProperty)
        }

        vbox {
            addClass("confirm-dialog__body")
            vgrow = Priority.ALWAYS

            label(messageTextProperty) {
                addClass("confirm-dialog__message")
            }

            scrollpane {
                addClass("confirm-dialog__details")
                scroll = this

                label(detailsTextProperty)

                visibleProperty().bind(detailsTextProperty.isNotEmpty)
                managedProperty().bind(visibleProperty())
            }
        }

        hbox {
            addClass("confirm-dialog__footer")

            button(cancelButtonTextProperty) {
                addClass("btn", "btn--secondary")

                hgrow = Priority.ALWAYS
                tooltip { textProperty().bind(this@button.textProperty()) }
                graphicProperty().bind(cancelButtonIconProperty)

                onActionProperty().bind(onCancelActionProperty())
                visibleProperty().bind(onCancelActionProperty.isNotNull)
                managedProperty().bind(visibleProperty())
            }

            button(confirmButtonTextProperty) {
                addClass("btn", "btn--primary")

                hgrow = Priority.ALWAYS
                tooltip { textProperty().bind(this@button.textProperty()) }
                graphicProperty().bind(confirmButtonIconProperty)

                onActionProperty().bind(onConfirmActionProperty())
                visibleProperty().bind(onConfirmActionProperty.isNotNull)
                managedProperty().bind(visibleProperty())
            }

            visibleProperty().bind(
                onCancelActionProperty.isNotNull.or(onConfirmActionProperty.isNotNull)
            )
            managedProperty().bind(visibleProperty())
        }
    }

    init {
        setContent(content)
    }

    override fun onDock() {
        scroll.apply {
            runLater { customizeScrollbarSkin() }
        }
    }

    fun setOnCancel(op: () -> Unit) {
        onCancelActionProperty.set(EventHandler { op.invoke() })
    }

    private fun onCancelActionProperty(): ObjectProperty<EventHandler<ActionEvent>> {
        return onCancelActionProperty
    }

    fun setOnConfirm(op: () -> Unit) {
        onConfirmActionProperty.set(EventHandler { op.invoke() })
    }

    private fun onConfirmActionProperty(): ObjectProperty<EventHandler<ActionEvent>> {
        return onConfirmActionProperty
    }
}

fun confirmDialog(setup: ConfirmDialog.() -> Unit = {}): ConfirmDialog {
    val confirmDialog = ConfirmDialog()
    confirmDialog.setup()
    return confirmDialog
}
