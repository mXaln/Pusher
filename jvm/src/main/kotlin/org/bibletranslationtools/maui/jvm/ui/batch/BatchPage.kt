package org.bibletranslationtools.maui.jvm.ui.batch

import javafx.beans.binding.Bindings
import javafx.event.EventHandler
import javafx.scene.control.TableView
import javafx.scene.input.DragEvent
import javafx.scene.input.TransferMode
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.bibletranslationtools.maui.common.data.Batch
import org.bibletranslationtools.maui.jvm.ListenerDisposer
import org.bibletranslationtools.maui.jvm.assets.AppResources
import org.bibletranslationtools.maui.jvm.controls.SearchBar
import org.bibletranslationtools.maui.jvm.controls.batchTableView
import org.bibletranslationtools.maui.jvm.controls.dialog.ConfirmDialog
import org.bibletranslationtools.maui.jvm.controls.dialog.ProgressDialog
import org.bibletranslationtools.maui.jvm.controls.dialog.confirmDialog
import org.bibletranslationtools.maui.jvm.controls.dialog.progressDialog
import org.bibletranslationtools.maui.jvm.controls.searchBar
import org.bibletranslationtools.maui.jvm.onSelectionChangeWithDisposer
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

    private val listeners = mutableListOf<ListenerDisposer>()

    private lateinit var batchContent: VBox
    private lateinit var tableView: TableView<Batch>
    private lateinit var searchBar: SearchBar

    private lateinit var confirmDialog: ConfirmDialog
    private lateinit var progressDialog: ProgressDialog

    init {
        importStylesheet(AppResources.load("/css/batch-page.css"))

        initializeConfirmDialog()
        initializeProgressDialog()
    }

    override val root = borderpane {
        top = mainHeader {
            uploadTargetProperty.bind(batchDataStore.uploadTargetProperty)
            appTitleProperty.bind(batchDataStore.appTitleProperty)
        }

        center = vbox {
            addClass("batch-page")

            batchContent = this

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
                        addClass("btn", "btn--primary")
                        graphic = FontIcon(MaterialDesign.MDI_DOWNLOAD)

                        action {
                            chooseFile(
                                FX.messages["importResourceFromZip"],
                                arrayOf(),
                                mode = FileChooserMode.Multi,
                                owner = currentWindow
                            ).also { viewModel.onDropFiles(it) }
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

                    button("confirm dialog") {
                        action {
                            confirmDialog.open()
                        }
                    }
                    button("progress dialog") {
                        action {
                            progressDialog.open()
                        }
                    }

                    region {
                        hgrow = Priority.ALWAYS
                    }

                    searchBar {
                        searchBar = this
                        promptText = messages["search"]
                        viewModel.searchQueryProperty.bind(textProperty())
                    }
                }

                batchTableView(viewModel.sortedBatches) {
                    tableView = this

                    emptyPromptProperty.set(messages["noBatchesPrompt"])
                    nameColumnProperty.set(messages["batchName"])
                    dateColumnProperty.set(messages["batchDate"])
                    deleteTextProperty.set(messages["deleteBatch"])

                    viewModel.sortedBatches.comparatorProperty().bind(comparatorProperty())
                }
            }
        }
    }

    override fun onDock() {
        tableView.onSelectionChangeWithDisposer {
            it?.let(viewModel::openBatch)
        }.also(listeners::add)
        viewModel.onDock()
    }

    override fun onUndock() {
        searchBar.text = ""
        listeners.forEach(ListenerDisposer::dispose)
        listeners.clear()
        viewModel.onUndock()
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
                viewModel.onDropFiles(it.dragboard.files)
                success = true
            }
            it.isDropCompleted = success
            it.consume()
        }
    }

    private fun initializeConfirmDialog() {
        confirmDialog {
            confirmDialog = this

            uploadTargetProperty.bind(batchDataStore.uploadTargetProperty)
            titleIconProperty.set(FontIcon(MaterialDesign.MDI_CHECK_CIRCLE))
            titleTextProperty.set("Export Successful")
            detailsTextProperty.set("You have successfully exported your files to:\n\n" +
                    "/Desktop/MAUI/Indonesian/amos-id-01202024.maui\n\n" +
                    "Do you wish to open your exported CSV file?\n\n" +
                    "You have successfully exported your files to:\n\n" +
                    "/Desktop/MAUI/Indonesian/amos-id-01202024.maui\n\n" +
                    "Do you wish to open your exported CSV file?\n\n" +
                    "You have successfully exported your files to:\n\n" +
                    "/Desktop/MAUI/Indonesian/amos-id-01202024.maui\n\n" +
                    "Do you wish to open your exported CSV file?\n\n" +
                    "You have successfully exported your files to:\n\n" +
                    "/Desktop/MAUI/Indonesian/amos-id-01202024.maui\n\n" +
                    "Do you wish to open your exported CSV file?\n\n")

            messageTextProperty.set("File exported!")
            cancelButtonTextProperty.set("Close")
            confirmButtonTextProperty.set("Open CSV")
            //confirmButtonTextProperty.set("OK")

            cancelButtonIconProperty.set(FontIcon(MaterialDesign.MDI_CLOSE_CIRCLE))
            confirmButtonIconProperty.set(FontIcon(MaterialDesign.MDI_OPEN_IN_APP))

            setOnConfirm {
                println("Confirmed")
                close()
            }

            setOnCancel {
                println("Cancelled")
                close()
            }
        }
    }

    private fun initializeProgressDialog() {
        progressDialog {
            progressDialog = this

            uploadTargetProperty.bind(batchDataStore.uploadTargetProperty)
            titleTextProperty.set("Exporting Files")
            messageTextProperty.set("Your files are being exported. This will take a moment.")
        }
    }
}