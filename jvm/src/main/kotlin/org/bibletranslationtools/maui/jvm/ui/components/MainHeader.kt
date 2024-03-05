package org.bibletranslationtools.maui.jvm.ui.components

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventTarget
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import org.bibletranslationtools.maui.jvm.assets.AppResources
import org.bibletranslationtools.maui.jvm.ui.UploadTarget
import org.bibletranslationtools.maui.jvm.ui.events.GoHomeEvent
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import tornadofx.*

class MainHeader : HBox() {

    val uploadTargetProperty = SimpleObjectProperty<UploadTarget>()
    val appTitleProperty = SimpleStringProperty()

    init {
        addClass("main-header")

        importStylesheet(AppResources.load("/css/main-header.css"))

        uploadTargetProperty.onChange {
            togglePseudoClass("accent", it == UploadTarget.DEV)
        }

        label {
            addClass("main-header-app-name__text")

            uploadTargetProperty.onChange {
                togglePseudoClass("accent", it == UploadTarget.DEV)
            }

            textProperty().bind(appTitleProperty)
        }

        region {
            hgrow = Priority.ALWAYS
        }

        label {
            addClass("main-header-home__icon")
            graphic = FontIcon(MaterialDesign.MDI_HOME)

            uploadTargetProperty.onChange {
                togglePseudoClass("accent", it == UploadTarget.DEV)
            }

            setOnMouseClicked {
                FX.eventbus.fire(GoHomeEvent())
            }

            visibleProperty().bind(uploadTargetProperty.isNotNull)
        }
    }
}

fun EventTarget.mainHeader(op: MainHeader.() -> Unit = {}): MainHeader {
    val mainHeader = MainHeader()
    return opcr(this, mainHeader, op)
}