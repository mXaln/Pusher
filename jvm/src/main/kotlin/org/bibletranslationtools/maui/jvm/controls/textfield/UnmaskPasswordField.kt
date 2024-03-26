package org.bibletranslationtools.maui.jvm.controls.textfield

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.event.EventTarget
import javafx.scene.layout.StackPane
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import tornadofx.addClass
import tornadofx.attachTo

class UnmaskPasswordField : StackPane() {
    val iconProperty = SimpleObjectProperty<FontIcon>()
    val promptTextProperty = SimpleStringProperty("")
    val textProperty = SimpleStringProperty()
    val onActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    private val showIcon = FontIcon(MaterialDesign.MDI_EYE)
    private val hideIcon = FontIcon(MaterialDesign.MDI_EYE_OFF)
    private val showPasswordProperty = SimpleBooleanProperty()

    init {
        addClass("wa-password-field")

        iconTextField {
            leftProperty().bind(iconProperty)
            visibleProperty().bind(showPasswordProperty)
            promptTextProperty().bind(promptTextProperty)
            textProperty().bindBidirectional(textProperty)
            onActionProperty().bind(onActionProperty)

            right = hideIcon.apply {
                setOnMouseClicked {
                    toggleShowIcon()
                }
            }
        }

        iconPasswordField {
            leftProperty().bind(iconProperty)
            visibleProperty().bind(showPasswordProperty.not())
            promptTextProperty().bind(promptTextProperty)
            textProperty().bindBidirectional(textProperty)
            onActionProperty().bind(onActionProperty)

            right = showIcon.apply {
                setOnMouseClicked {
                    toggleShowIcon()
                }
            }
        }
    }

    private fun toggleShowIcon() {
        showPasswordProperty.set(showPasswordProperty.value.not())
    }
}

fun EventTarget.unmaskPasswordField(op: UnmaskPasswordField.() -> Unit = {}) =
    UnmaskPasswordField().attachTo(this, op)