package org.bibletranslationtools.maui.jvm.ui.batch

import javafx.beans.binding.Bindings
import javafx.event.EventHandler
import javafx.scene.input.DragEvent
import javafx.scene.input.TransferMode
import javafx.scene.layout.Priority
import org.bibletranslationtools.maui.jvm.assets.AppResources
import org.bibletranslationtools.maui.jvm.controls.batchTableView
import org.bibletranslationtools.maui.jvm.controls.searchBar
import org.bibletranslationtools.maui.jvm.onChangeAndDoNow
import org.bibletranslationtools.maui.jvm.ui.BatchDataStore
import org.bibletranslationtools.maui.jvm.ui.UploadTarget
import org.bibletranslationtools.maui.jvm.ui.components.mainHeader
import org.bibletranslationtools.maui.jvm.ui.components.uploadTargetHeader
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import tornadofx.*

class BatchPage : View() {
    private val viewModel: BatchViewModel by inject()
    private val batchDataStore: BatchDataStore by inject()

    init {
        importStylesheet(AppResources.load("/css/batch-page.css"))
    }

    override val root = borderpane {
        top = mainHeader {
            uploadTargetProperty.bind(batchDataStore.uploadTargetProperty)
            appTitleProperty.bind(batchDataStore.appTitleProperty)
        }

        center = vbox {
            addClass("batch-page")

            batchDataStore.uploadTargetProperty.onChangeAndDoNow {
                togglePseudoClass("accent", it == UploadTarget.DEV)
            }

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
                addClass("batch-page__contents")

                vgrow = Priority.ALWAYS

                hbox {
                    addClass("batch-page__controls")

                    button(messages["importFiles"]) {
                        addClass("btn", "btn--secondary", "batch--btn")
                        graphic = FontIcon(MaterialDesign.MDI_DOWNLOAD)

                        action {
                            chooseFile(
                                FX.messages["importResourceFromZip"],
                                arrayOf(),
                                mode = FileChooserMode.Multi,
                                owner = currentWindow
                            ).also { viewModel.importFiles(it) }
                        }
                    }
                }

                vbox {
                    addClass("batch__import")

                    label {
                        addClass("batch__import__icon")
                        graphic = FontIcon(MaterialDesign.MDI_DOWNLOAD)
                    }

                    label(messages["importFilesStartProject"]) {
                        addClass("batch__import__title")
                    }

                    label(messages["dropFiles"]) {
                        addClass("batch__import__subtitle")
                    }

                    setOnDragExited {
                        togglePseudoClass("drag-over", false)
                    }

                    setOnDragOver {
                        if (it.dragboard.hasFiles()) {
                            togglePseudoClass("drag-over", true)
                        }
                        onDragOverHandler().handle(it)
                    }
                    onDragDropped = onDragDroppedHandler()
                }

                hbox {
                    addClass("batch__search")

                    label(messages["batches"]) {
                        addClass("batch__search__title")
                    }

                    region {
                        hgrow = Priority.ALWAYS
                    }

                    searchBar {
                        promptText = messages["search"]
                        viewModel.searchQueryProperty.bind(textProperty())
                    }
                }

                batchTableView(viewModel.sortedBatches) {
                    noBatchesPromptProperty.set(messages["noBatchesPrompt"])
                    batchNameLabelProperty.set(messages["batchName"])
                    batchDateLabelProperty.set(messages["batchDate"])
                    deleteTextProperty.set(messages["deleteBatch"])

                    uploadTargetProperty.bind(batchDataStore.uploadTargetProperty)
                    viewModel.sortedBatches.comparatorProperty().bind(comparatorProperty())

                    batchDataStore.activeBatchProperty.bind(selectionModel.selectedItemProperty())
                }
            }
        }
    }

    private fun onDragOverHandler(): EventHandler<DragEvent> {
        return EventHandler {
            if (it.gestureSource != this && it.dragboard.hasFiles()) {
                it.acceptTransferModes(TransferMode.COPY)
            }
            it.consume()
        }
    }

    private fun onDragDroppedHandler(): EventHandler<DragEvent> {
        return EventHandler {
            var success = false
            if (it.dragboard.hasFiles()) {
                viewModel.importFiles(it.dragboard.files)
                success = true
            }
            it.isDropCompleted = success
            it.consume()
        }
    }
}