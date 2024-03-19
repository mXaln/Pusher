package org.bibletranslationtools.maui.jvm.ui.upload

import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.control.ContentDisplay
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.Priority
import org.bibletranslationtools.maui.common.data.Batch
import org.bibletranslationtools.maui.jvm.assets.AppResources
import org.bibletranslationtools.maui.jvm.controls.dialog.*
import org.bibletranslationtools.maui.jvm.controls.mediatableview.mediaTableView
import org.bibletranslationtools.maui.jvm.ui.UploadTarget
import org.bibletranslationtools.maui.jvm.ui.components.mainHeader
import org.bibletranslationtools.maui.jvm.ui.components.uploadTargetHeader
import org.bibletranslationtools.maui.jvm.ui.events.*
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import tornadofx.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class UploadPage : View() {

    private val viewModel: UploadMediaViewModel by inject()

    private lateinit var confirmDialog: ConfirmDialog
    private lateinit var progressDialog: ProgressDialog

    private lateinit var nameLabel: Label
    private lateinit var nameTextEdit: TextField
    private val nameEditingProperty = SimpleBooleanProperty()

    init {
        importStylesheet(AppResources.load("/css/upload-page.css"))

        initializeConfirmDialog()
        initializeProgressDialog()

        subscribe<AppSaveRequestEvent> {
            viewModel.saveBatch()
        }

        subscribe<ConfirmDialogEvent> {
            openConfirmDialog(it)
        }

        subscribe<ProgressDialogEvent> {
            openProgressDialog(it)
        }

        subscribe<ErrorOccurredEvent> {
            val event = ConfirmDialogEvent(DialogType.ERROR, messages["errorOccurred"], it.message)
            openErrorDialog(event)
        }

        subscribe<ShowInfoEvent> {
            val event = ConfirmDialogEvent(DialogType.INFO, messages["information"], it.message)
            openSuccessDialog(event)
        }
    }

    override val root = borderpane {
        top = mainHeader {
            uploadTargetProperty.bind(viewModel.uploadTargetProperty)
            appTitleProperty.bind(viewModel.appTitleProperty)
        }

        center = vbox {
            addClass("upload-page")

            uploadTargetHeader {
                uploadTargetProperty.bindBidirectional(viewModel.uploadTargetProperty)
                Bindings.bindContent(uploadTargets, viewModel.uploadTargets)

                uploadTargetTextProperty.bind(viewModel.uploadTargetProperty.stringBinding {
                    when (it) {
                        UploadTarget.DEV -> messages["targetDev"]
                        UploadTarget.PROD -> messages["targetProd"]
                        else -> ""
                    }
                })
            }

            vbox {
                addClass("root-container")

                vgrow = Priority.ALWAYS

                hbox {
                    addClass("controls")

                    vbox {
                        spacing = 4.0
                        hgrow = Priority.ALWAYS

                        hbox {
                            label {
                                addClass("controls-title")
                                hgrow = Priority.ALWAYS
                                nameLabel = this

                                textProperty().bind(viewModel.activeBatchProperty.stringBinding { it?.name })
                                graphic = FontIcon(MaterialDesign.MDI_PENCIL)
                                contentDisplay = ContentDisplay.RIGHT

                                setOnMouseClicked {
                                    nameEditingProperty.set(true)
                                }

                                visibleProperty().bind(nameEditingProperty.not())
                                managedProperty().bind(visibleProperty())
                            }
                            hbox {
                                addClass("name-edit")
                                hgrow = Priority.ALWAYS

                                textfield {
                                    addClass("name-edit-input")
                                    hgrow = Priority.ALWAYS

                                    nameTextEdit = this
                                    nameEditingProperty.onChange { editing ->
                                        if (editing) {
                                            text = viewModel.activeBatchProperty.value?.name
                                            requestFocus()
                                            selectAll()
                                        }
                                    }

                                    maxWidthProperty().bind(nameLabel.widthProperty())

                                    action {
                                        nameEditingProperty.set(false)
                                        viewModel.onNameChanged(text)
                                    }
                                }
                                button(messages["save"]) {
                                    addClass("btn", "btn--icon", "btn--edit")
                                    graphic = FontIcon(MaterialDesign.MDI_CHECK)

                                    action {
                                        nameEditingProperty.set(false)
                                        viewModel.onNameChanged(nameTextEdit.text)
                                    }
                                }

                                visibleProperty().bind(nameEditingProperty)
                                managedProperty().bind(visibleProperty())
                            }
                        }

                        label {
                            addClass("controls-subtitle")
                            textProperty().bind(viewModel.activeBatchProperty.stringBinding {
                                it?.let { batch ->
                                    val parsed = LocalDateTime.parse(batch.created)
                                    val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a")
                                    parsed.format(formatter)
                                } ?: ""
                            })
                        }
                    }

                    region {
                        hgrow = Priority.ALWAYS
                    }

                    button(messages["saveBatch"]) {
                        addClass("btn", "btn--primary")
                        graphic = FontIcon(MaterialDesign.MDI_CONTENT_SAVE)

                        action {
                            viewModel.saveBatch()
                        }

                        enableWhen(viewModel.shouldSaveProperty)
                    }

                    button(messages["viewUploadedFiles"]) {
                        addClass("btn", "btn--secondary")
                        graphic = FontIcon(MaterialDesign.MDI_EXPORT)

                        action {
                            viewModel.viewUploadedFiles()
                        }

                        isDisable = true
                    }

                    button(messages["exportCsv"]) {
                        addClass("btn", "btn--secondary")
                        graphic = FontIcon(MaterialDesign.MDI_FILE_EXPORT)

                        action {
                            viewModel.exportCsv()
                        }

                        enableWhen {
                            viewModel.tableMediaItems.booleanBinding {
                                it.isNotEmpty()
                            }
                        }
                    }
                }

                mediaTableView(viewModel.tableMediaItems) {
                    languagesProperty.set(viewModel.languages)
                    resourceTypesProperty.set(viewModel.resourceTypes)
                    booksProperty.set(viewModel.books)
                    mediaExtensionsProperty.set(viewModel.mediaExtensions)
                    mediaQualitiesProperty.set(viewModel.mediaQualities)
                    groupingsProperty.set(viewModel.groupings)
                    statusFilterProperty.set(viewModel.statusFilter)
                }
            }
        }

        bottom = hbox {
            addClass("upload-footer")

            button(messages["removeSelected"]) {
                addClass("btn", "btn--secondary")
                graphic = FontIcon(MaterialDesign.MDI_DELETE)

                action {
                    viewModel.removeSelected()
                }

                enableWhen {
                    viewModel.tableMediaItems.booleanBinding {
                        it.any { item -> item.selected }
                    }
                }
            }

            region {
                hgrow = Priority.ALWAYS
            }

            button(messages["verify"]) {
                addClass("btn", "btn--secondary")
                graphic = FontIcon(MaterialDesign.MDI_CHECK)

                action {
                    viewModel.verify()
                }

                enableWhen {
                    viewModel.tableMediaItems.booleanBinding {
                        it.any { item -> item.selected }
                    }
                }
            }

            button(messages["upload"]) {
                addClass("btn", "btn--primary")
                graphic = FontIcon(MaterialDesign.MDI_ARROW_UP)

                action {
                    viewModel.upload()
                }

                enableWhen {
                    viewModel.tableMediaItems.booleanBinding {
                        it.any { item -> item.selected }
                    }
                }
            }
        }
    }

    override fun onDock() {
        viewModel.onDock()
        nameEditingProperty.set(false)
    }

    override fun onUndock() {
        viewModel.onUndock()
    }

    private fun initializeConfirmDialog() {
        confirmDialog {
            confirmDialog = this
            uploadTargetProperty.bind(viewModel.uploadTargetProperty)
        }
    }

    private fun openConfirmDialog(event: ConfirmDialogEvent) {
        resetConfirmDialog()
        when (event.type) {
            DialogType.INFO -> openSuccessDialog(event)
            DialogType.ERROR -> openErrorDialog(event)
            else -> {}
        }
    }

    private fun openSuccessDialog(event: ConfirmDialogEvent) {
        confirmDialog.apply {
            alertProperty.set(false)
            titleTextProperty.set(event.title)
            messageTextProperty.set(event.message)
            detailsTextProperty.set(event.details)
            primaryButtonTextProperty.set(messages["ok"])
            setOnPrimaryAction { close() }
            open()
        }
    }

    private fun openErrorDialog(event: ConfirmDialogEvent) {
        confirmDialog.apply {
            alertProperty.set(true)
            titleTextProperty.set(event.title)
            messageTextProperty.set(event.message)
            detailsTextProperty.set(event.details)
            primaryButtonTextProperty.set(messages["ok"])
            setOnPrimaryAction { close() }
            open()
        }
    }

    private fun openDeleteDialog(batch: Batch) {
        /*resetConfirmDialog()
        confirmDialog.apply {
            alertProperty.set(true)
            titleTextProperty.set(messages["deleteBatch"])
            messageTextProperty.set(messages["deleteBatchWarning"])
            detailsTextProperty.set(messages["wishToContinue"])
            primaryButtonTextProperty.set(messages["cancel"])
            primaryButtonIconProperty.set(FontIcon(MaterialDesign.MDI_CLOSE_CIRCLE))
            secondaryButtonTextProperty.set(messages["delete"])
            secondaryButtonIconProperty.set(FontIcon(MaterialDesign.MDI_DELETE))

            setOnPrimaryAction { close() }
            setOnSecondaryAction {
                close()
                viewModel.deleteBatch(batch)
            }
            open()
        }*/
    }

    private fun resetConfirmDialog() {
        confirmDialog.apply {
            alertProperty.set(false)
            titleTextProperty.set(null)
            messageTextProperty.set(null)
            detailsTextProperty.set(null)
            primaryButtonTextProperty.set(null)
            primaryButtonIconProperty.set(null)
            onPrimaryActionProperty.set(null)
            secondaryButtonTextProperty.set(null)
            secondaryButtonIconProperty.set(null)
            onSecondaryActionProperty.set(null)
        }
    }

    private fun initializeProgressDialog() {
        progressDialog {
            progressDialog = this
            uploadTargetProperty.bind(viewModel.uploadTargetProperty)
        }
    }

    private fun openProgressDialog(event: ProgressDialogEvent) {
        progressDialog.apply {
            titleTextProperty.set(event.title)
            messageTextProperty.set(event.message)
            if (event.show) open() else close()
        }
    }
}