package org.bibletranslationtools.maui.jvm.controls.dialog

import javafx.animation.Interpolator
import javafx.animation.RotateTransition
import javafx.animation.Timeline
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.layout.Priority
import javafx.scene.transform.Rotate
import javafx.util.Duration
import org.bibletranslationtools.maui.jvm.onChangeAndDoNow
import org.bibletranslationtools.maui.jvm.ui.UploadTarget
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import tornadofx.*

class ProgressDialog : OtterDialog() {
    
    val titleTextProperty = SimpleStringProperty()
    val messageTextProperty = SimpleStringProperty()
    val uploadTargetProperty = SimpleObjectProperty<UploadTarget>()

    private lateinit var rotateAnimation: RotateTransition

    private val content = vbox {
        addClass("progress-dialog")

        uploadTargetProperty.onChangeAndDoNow {
            togglePseudoClass("accent", it == UploadTarget.DEV)
        }

        hbox {
            addClass("progress-dialog__header")

            label {
                graphic = FontIcon(MaterialDesign.MDI_AUTORENEW).apply {
                    rotate(Duration.millis(1000.0), 360.0, play = false) {
                        rotateAnimation = this
                        axis = Rotate.Z_AXIS
                        cycleCount = Timeline.INDEFINITE
                        interpolator = Interpolator.LINEAR
                    }
                }
            }

            label(titleTextProperty)
        }
        vbox {
            addClass("progress-dialog__body")
            vgrow = Priority.ALWAYS
            label {
                addClass("progress-dialog__message")
                textProperty().bind(messageTextProperty)
            }
        }
    }

    init {
        setContent(content)
    }

    override fun onDock() {
        runLater {
            rotateAnimation.play()
        }
    }

    override fun onUndock() {
        runLater {
            rotateAnimation.pause()
        }
    }
}

fun progressDialog(op: ProgressDialog.() -> Unit) = ProgressDialog().apply(op)