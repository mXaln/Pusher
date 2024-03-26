package org.bibletranslationtools.maui.jvm.controls.textfield

import javafx.event.EventTarget
import org.controlsfx.control.textfield.CustomPasswordField
import tornadofx.addClass
import tornadofx.attachTo

class IconPasswordField : CustomPasswordField() {
    init {
        addClass("wa-text-field", "wa-icon-text-field")
    }
}

fun EventTarget.iconPasswordField(op: IconPasswordField.() -> Unit = {}) =
    IconPasswordField().attachTo(this, op)
