package org.bibletranslationtools.maui.jvm.ui.startup

import javafx.geometry.Pos
import org.bibletranslationtools.maui.jvm.assets.AppResources
import org.bibletranslationtools.maui.jvm.ui.UploadTarget
import tornadofx.*

class StartupPage : View() {
    private val viewModel: StartupViewModel by inject()

    init {
        importStylesheet(AppResources.load("/css/root.css"))
        importStylesheet(AppResources.load("/css/buttons.css"))
        importStylesheet(AppResources.load("/css/startup.css"))
    }

    override val root = vbox {
        addClass("startup__page")

        label(messages["appName"]) {
            addClass("app-name__text")
        }

        vbox {
            addClass("upload-target")

            label(messages["chooseUploadTarget"]) {
                addClass("upload-target__text")
            }

            button(messages["targetDev"]) {
                addClass(
                    "btn",
                    "btn--primary-inverted",
                    "upload-target__btn",
                    "upload-target__btn--dev"
                )
                action {
                    viewModel.selectUploadTarget(UploadTarget.DEV)
                }
            }

            button(messages["targetProd"]) {
                addClass(
                    "btn",
                    "btn--primary-inverted",
                    "upload-target__btn",
                    "upload-target__btn--prod"
                )
                action {
                    viewModel.selectUploadTarget(UploadTarget.PROD)
                }
            }
        }
    }
}