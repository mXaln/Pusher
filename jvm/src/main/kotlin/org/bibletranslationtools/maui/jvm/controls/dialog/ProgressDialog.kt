package org.bibletranslationtools.maui.jvm.controls.dialog

import javafx.animation.Interpolator
import javafx.animation.RotateTransition
import javafx.animation.Timeline
import javafx.beans.property.SimpleStringProperty
import javafx.scene.layout.Priority
import javafx.scene.transform.Rotate
import javafx.util.Duration
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import tornadofx.*

class ProgressDialog : MauiDialog() {
    
    val titleTextProperty = SimpleStringProperty()
    val messageTextProperty = SimpleStringProperty()

    private lateinit var rotateAnimation: RotateTransition

    private val content = vbox {
        addClass("progress-dialog")

        hbox {
            addClass("header")

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
            addClass("body")
            vgrow = Priority.ALWAYS
            label {
                addClass("message")
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