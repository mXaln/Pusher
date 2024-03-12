package org.bibletranslationtools.maui.jvm.ui.upload

import javafx.beans.binding.Bindings
import javafx.scene.layout.Priority
import org.bibletranslationtools.maui.jvm.assets.AppResources
import org.bibletranslationtools.maui.jvm.controls.mediaTableView
import org.bibletranslationtools.maui.jvm.ui.events.AppSaveRequestEvent
import org.bibletranslationtools.maui.jvm.ui.BatchDataStore
import org.bibletranslationtools.maui.jvm.ui.UploadTarget
import org.bibletranslationtools.maui.jvm.ui.components.mainHeader
import org.bibletranslationtools.maui.jvm.ui.components.uploadTargetHeader
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import tornadofx.*

class UploadPage : View() {

    private val viewModel: UploadMediaViewModel by inject()
    private val batchDataStore: BatchDataStore by inject()

    init {
        importStylesheet(AppResources.load("/css/upload-page.css"))

        subscribe<AppSaveRequestEvent> {
            viewModel.saveBatch()
        }
    }

    override val root = borderpane {
        top = mainHeader {
            uploadTargetProperty.bind(batchDataStore.uploadTargetProperty)
            appTitleProperty.bind(batchDataStore.appTitleProperty)
        }

        center = vbox {
            addClass("upload-page")

            uploadTargetHeader {
                uploadTargetProperty.bindBidirectional(batchDataStore.uploadTargetProperty)
                Bindings.bindContent(uploadTargets, batchDataStore.uploadTargets)

                uploadTargetTextProperty.bind(batchDataStore.uploadTargetProperty.stringBinding {
                    when (it) {
                        UploadTarget.DEV -> messages["targetDev"]
                        UploadTarget.PROD -> messages["targetProd"]
                        else -> ""
                    }
                })
                changeUploadTargetTextProperty.set(messages["changeUploadTarget"])
            }

            vbox {
                addClass("upload-page__contents")

                vgrow = Priority.ALWAYS

                hbox {
                    addClass("upload-page__controls")

                    label {
                        addClass("upload-page__controls-title")
                        textProperty().bind(batchDataStore.activeBatchProperty.stringBinding { it?.name })
                    }

                    region {
                        hgrow = Priority.ALWAYS
                    }

                    button(messages["saveBatch"]) {
                        addClass("btn", "btn--secondary", "upload-page--btn")
                        graphic = FontIcon(MaterialDesign.MDI_CONTENT_SAVE)

                        action {
                            println("Save project")
                        }
                    }

                    button(messages["viewUploadedFiles"]) {
                        addClass("btn", "btn--secondary", "upload-page--btn")
                        graphic = FontIcon(MaterialDesign.MDI_EXPORT)

                        action {
                            println("View uploaded files")
                        }
                    }

                    button(messages["exportCsv"]) {
                        addClass("btn", "btn--secondary", "upload-page--btn")
                        graphic = FontIcon(MaterialDesign.MDI_FILE_EXPORT)

                        action {
                            println("Export CSV")
                        }
                    }
                }

                mediaTableView(viewModel.sortedMediaItems) {
                    emptyPromptProperty.set(messages["noMediaPrompt"])

                    fileNameColumnProperty.set(messages["fileName"])
                    languageColumnProperty.set(messages["language"])
                    resourceTypeColumnProperty.set(messages["resourceType"])
                    bookColumnProperty.set(messages["book"])
                    chapterColumnProperty.set(messages["chapter"])
                    mediaExtensionColumnProperty.set(messages["mediaExtension"])
                    mediaQualityColumnProperty.set(messages["mediaQuality"])
                    groupingColumnProperty.set(messages["grouping"])
                    statusColumnProperty.set(messages["status"])

                    viewModel.sortedMediaItems.comparatorProperty().bind(comparatorProperty())
                }
            }
        }
    }

    override fun onDock() {
        viewModel.onDock()
    }

    override fun onUndock() {
        viewModel.onUndock()
    }
}