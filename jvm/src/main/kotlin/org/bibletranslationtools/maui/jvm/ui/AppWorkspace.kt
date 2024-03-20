package org.bibletranslationtools.maui.jvm.ui

import org.bibletranslationtools.maui.jvm.assets.AppResources
import org.bibletranslationtools.maui.jvm.controls.dialog.*
import org.bibletranslationtools.maui.jvm.onChangeAndDoNow
import org.bibletranslationtools.maui.jvm.ui.startup.StartupPage
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import tornadofx.*

class AppWorkspace : Workspace() {

    private val navigator: NavigationMediator by inject()
    private val batchDataStore: BatchDataStore by inject()

    private lateinit var confirmDialog: ConfirmDialog
    private lateinit var progressDialog: ProgressDialog

    init {
        header.removeFromParent()

        importStylesheet(AppResources.load("/css/root.css"))
        importStylesheet(AppResources.load("/css/target/dev-target.css"))
        importStylesheet(AppResources.load("/css/target/prod-target.css"))

        importStylesheet(AppResources.load("/css/control.css"))
        importStylesheet(AppResources.load("/css/buttons.css"))

        bindUploadTargetClassToRoot()

        initializeConfirmDialog()
        initializeProgressDialog()

        subscribe<ConfirmDialogEvent> {
            openConfirmDialog(it)
        }

        subscribe<ProgressDialogEvent> {
            openProgressDialog(it)
        }
    }

    override fun onBeforeShow() {
        navigator.dock<StartupPage>()
    }

    private fun bindUploadTargetClassToRoot() {
        batchDataStore.uploadTargetProperty.onChangeAndDoNow {
            when (it) {
                UploadTarget.DEV -> {
                    root.addClass(UploadTarget.DEV.styleClass)
                    root.removeClass(UploadTarget.PROD.styleClass)
                }
                else -> {
                    root.addClass(UploadTarget.PROD.styleClass)
                    root.removeClass(UploadTarget.DEV.styleClass)
                }
            }
        }
    }

    private fun initializeConfirmDialog() {
        confirmDialog {
            confirmDialog = this
            uploadTargetProperty.bind(batchDataStore.uploadTargetProperty)
        }
    }

    private fun openConfirmDialog(event: ConfirmDialogEvent) {
        resetConfirmDialog()
        when (event.type) {
            DialogType.INFO -> openInfoDialog(event)
            DialogType.ERROR -> openErrorDialog(event)
            DialogType.DELETE -> openDeleteDialog(event)
            else -> {}
        }
    }

    private fun openInfoDialog(event: ConfirmDialogEvent) {
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

    private fun openDeleteDialog(event: ConfirmDialogEvent) {
        resetConfirmDialog()
        confirmDialog.apply {
            alertProperty.set(true)
            titleTextProperty.set(event.title)
            messageTextProperty.set(event.message)
            detailsTextProperty.set(event.details)
            primaryButtonTextProperty.set(messages["cancel"])
            primaryButtonIconProperty.set(FontIcon(MaterialDesign.MDI_CLOSE_CIRCLE))
            secondaryButtonTextProperty.set(messages["delete"])
            secondaryButtonIconProperty.set(FontIcon(MaterialDesign.MDI_DELETE))

            setOnPrimaryAction {
                event.primaryAction()
                close()
            }
            setOnSecondaryAction {
                event.secondaryAction()
                close()
            }
            open()
        }
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
            uploadTargetProperty.bind(batchDataStore.uploadTargetProperty)
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