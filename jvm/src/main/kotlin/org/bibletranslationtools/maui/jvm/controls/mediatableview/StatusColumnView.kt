package org.bibletranslationtools.maui.jvm.controls.mediatableview

import javafx.event.EventTarget
import javafx.scene.control.Tooltip
import javafx.scene.layout.HBox
import org.bibletranslationtools.maui.common.data.FileStatus
import org.bibletranslationtools.maui.jvm.onChangeAndDoNow
import org.bibletranslationtools.maui.jvm.ui.MediaItem
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import tornadofx.*

class StatusColumnView(mediaItem: MediaItem) : HBox() {
    init {
        addClass("status")

        mediaItem.statusProperty.onChangeAndDoNow {
            togglePseudoClass("error", it == FileStatus.REJECTED)
        }

        label {
            addClass("status-alert")
            graphicProperty().bind(mediaItem.statusProperty.objectBinding {
                if (it == FileStatus.REJECTED) {
                    FontIcon(MaterialDesign.MDI_ALERT)
                } else null
            })
            tooltipProperty().bind(mediaItem.statusProperty.objectBinding {
                if (it == FileStatus.REJECTED) {
                    Tooltip().apply {
                        textProperty().bind(mediaItem.statusMessageProperty)
                    }
                } else null
            })
        }

        label {
            addClass("status-title")
            textProperty().bind(mediaItem.statusProperty.stringBinding { it?.status ?: "" })
            tooltipProperty().bind(mediaItem.statusProperty.objectBinding {
                if (it == FileStatus.REJECTED) {
                    Tooltip().apply {
                        textProperty().bind(mediaItem.statusMessageProperty)
                    }
                } else null
            })
        }
    }
}

fun EventTarget.statusColumnView(mediaItem: MediaItem, op: StatusColumnView.() -> Unit = {}) =
    StatusColumnView(mediaItem).attachTo(this, op)