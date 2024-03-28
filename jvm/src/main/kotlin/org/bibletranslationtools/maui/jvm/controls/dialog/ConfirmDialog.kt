package org.bibletranslationtools.maui.jvm.controls.dialog

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.Parent
import javafx.scene.control.ScrollPane
import javafx.scene.layout.Priority
import org.bibletranslationtools.maui.jvm.assets.AppResources
import org.bibletranslationtools.maui.jvm.customizeScrollbarSkin
import org.bibletranslationtools.maui.jvm.onChangeAndDoNow
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import tornadofx.*

enum class DialogType {
    INFO,
    ERROR,
    DELETE,
    CONFIRM
}

class ConfirmDialog : MauiDialog() {
    val titleTextProperty = SimpleStringProperty()
    val messageTextProperty = SimpleStringProperty()
    val detailsTextProperty = SimpleStringProperty()
    val primaryButtonTextProperty = SimpleStringProperty()
    val primaryButtonIconProperty = SimpleObjectProperty<FontIcon>()
    val secondaryButtonTextProperty = SimpleStringProperty()
    val secondaryButtonIconProperty = SimpleObjectProperty<FontIcon>()
    val alertProperty = SimpleBooleanProperty()

    val onPrimaryActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    val onSecondaryActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    private val normalIcon = FontIcon(MaterialDesign.MDI_CHECK_CIRCLE)
    private val alertIcon = FontIcon(MaterialDesign.MDI_ALERT)

    private lateinit var scroll: Parent

    private val content = vbox {
        addClass("confirm-dialog")

        alertProperty.onChangeAndDoNow {
            togglePseudoClass("alert", it == true)
        }

        hbox {
            addClass("header")

            label {
                graphicProperty().bind(alertProperty.objectBinding {
                    if (it == true) alertIcon else normalIcon
                })
            }

            label(titleTextProperty)
        }

        vbox {
            addClass("body")
            vgrow = Priority.ALWAYS

            label(messageTextProperty) {
                addClass("message")
            }

            scrollpane {
                addClass("details")
                scroll = this

                isFitToWidth = true
                hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
                vgrow = Priority.ALWAYS

                text(detailsTextProperty).apply {
                    wrappingWidthProperty().bind(this@scrollpane.widthProperty().minus(20.0))
                }

                visibleProperty().bind(detailsTextProperty.isNotEmpty)
                managedProperty().bind(visibleProperty())
            }
        }

        hbox {
            addClass("footer")

            button(secondaryButtonTextProperty) {
                addClass("btn", "btn--secondary")

                hgrow = Priority.ALWAYS
                tooltip { textProperty().bind(this@button.textProperty()) }
                graphicProperty().bind(secondaryButtonIconProperty)

                onActionProperty().bind(onSecondaryActionProperty)
                visibleProperty().bind(onSecondaryActionProperty.isNotNull)
                managedProperty().bind(visibleProperty())
            }

            button(primaryButtonTextProperty) {
                addClass("btn", "btn--primary")

                hgrow = Priority.ALWAYS
                tooltip { textProperty().bind(this@button.textProperty()) }
                graphicProperty().bind(primaryButtonIconProperty)

                onActionProperty().bind(onPrimaryActionProperty)
                visibleProperty().bind(onPrimaryActionProperty.isNotNull)
                managedProperty().bind(visibleProperty())
            }

            visibleProperty().bind(
                onSecondaryActionProperty.isNotNull.or(onPrimaryActionProperty.isNotNull)
            )
            managedProperty().bind(visibleProperty())
        }
    }

    init {
        importStylesheet(AppResources.load("/css/confirm-dialog.css"))

        setContent(content)
    }

    override fun onDock() {
        scroll.apply {
            runLater { customizeScrollbarSkin() }
        }
    }

    fun setOnSecondaryAction(op: () -> Unit) {
        onSecondaryActionProperty.set(EventHandler { op.invoke() })
    }

    fun setOnPrimaryAction(op: () -> Unit) {
        onPrimaryActionProperty.set(EventHandler { op.invoke() })
    }
}

fun confirmDialog(setup: ConfirmDialog.() -> Unit = {}): ConfirmDialog {
    val confirmDialog = ConfirmDialog()
    confirmDialog.setup()
    return confirmDialog
}

class ConfirmDialogEvent(
    val type: DialogType,
    val title: String,
    val message: String,
    val details: String? = null,
    val primaryAction: () -> Unit = {},
    val secondaryAction: () -> Unit = {}
) : FXEvent()
