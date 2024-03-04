package org.bibletranslationtools.maui.jvm.ui.batch

import javafx.beans.binding.Bindings
import javafx.event.EventHandler
import javafx.scene.control.TableView
import javafx.scene.input.DragEvent
import javafx.scene.input.TransferMode
import javafx.scene.layout.Priority
import org.bibletranslationtools.maui.jvm.assets.AppResources
import org.bibletranslationtools.maui.jvm.onChangeAndDoNow
import org.bibletranslationtools.maui.jvm.ui.BatchDataStore
import org.bibletranslationtools.maui.jvm.ui.UploadTarget
import org.bibletranslationtools.maui.jvm.ui.components.mainHeader
import org.bibletranslationtools.maui.jvm.ui.components.uploadTargetHeader
import org.controlsfx.control.textfield.CustomTextField
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import tornadofx.*
import java.time.format.DateTimeFormatter

class BatchPage : View() {
    private val viewModel: BatchViewModel by inject()
    private val batchDataStore: BatchDataStore by inject()

    init {
        importStylesheet(AppResources.load("/css/batch.css"))
    }

    override val root = borderpane {
        top = mainHeader {
            uploadTargetProperty.bind(batchDataStore.uploadTargetProperty)
            appTitleProperty.bind(batchDataStore.appTitleProperty)
        }

        center = vbox {
            addClass("batch-page")

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
                    addClass("batch__controls")

                    button(messages["importFiles"]) {
                        addClass("btn", "btn--secondary", "btn--import")
                        graphic = FontIcon(MaterialDesign.MDI_DOWNLOAD)

                        batchDataStore.uploadTargetProperty.onChangeAndDoNow {
                            togglePseudoClass("accent", it == UploadTarget.DEV)
                        }

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
                    addClass("import-files")

                    label {
                        addClass("import-files__icon")
                        graphic = FontIcon(MaterialDesign.MDI_DOWNLOAD)
                    }

                    label(messages["importFilesStartProject"]) {
                        addClass("import-files__title")
                    }

                    label(messages["dropFiles"]) {
                        addClass("import-files__subtitle")
                    }

                    batchDataStore.uploadTargetProperty.onChangeAndDoNow {
                        togglePseudoClass("accent", it == UploadTarget.DEV)
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
                    addClass("batch-search")

                    label(messages["batches"]) {
                        addClass("batch-search__title")
                    }

                    region {
                        hgrow = Priority.ALWAYS
                    }

                    add(CustomTextField().apply {
                        addClass("batch-search__input")

                        right = FontIcon(MaterialDesign.MDI_MAGNIFY)
                        promptText = messages["search"]
                    })
                }

                tableview(batchDataStore.batches) {
                    addClass("batch-list")

                    vgrow = Priority.ALWAYS
                    columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
                    placeholder = borderpane {
                        center = vbox {
                            addClass("batch-list__placeholder")

                            label {
                                addClass("batch-list__placeholder-icon")
                                graphic = FontIcon(MaterialDesign.MDI_FOLDER_OUTLINE)
                            }
                            label(messages["noBatchesPrompt"]) {
                                addClass("batch-list__placeholder-text")
                            }
                        }
                    }

                    column(messages["batchName"], String::class) {
                        setCellValueFactory { it.value.name.toProperty() }
                    }
                    column(messages["batchDate"], String::class) {
                        setCellValueFactory {
                            val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss a")
                            it.value.created.format(formatter).toProperty()
                        }
                    }
                    column("", String::class)
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