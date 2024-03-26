package org.bibletranslationtools.maui.jvm.controls.textfield

import javafx.event.EventTarget
import org.controlsfx.control.textfield.CustomTextField
import tornadofx.addClass
import tornadofx.attachTo

class IconTextField : CustomTextField() {
    init {
        addClass("wa-text-field", "wa-icon-text-field")
    }
}

fun EventTarget.iconTextField(op: IconTextField.() -> Unit = {}) =
    IconTextField().attachTo(this, op)