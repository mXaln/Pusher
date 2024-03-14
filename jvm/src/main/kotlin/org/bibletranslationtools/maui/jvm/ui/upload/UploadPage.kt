package org.bibletranslationtools.maui.jvm.ui.upload

import javafx.beans.binding.Bindings
import javafx.scene.layout.Priority
import org.bibletranslationtools.maui.jvm.assets.AppResources
import org.bibletranslationtools.maui.jvm.controls.mediatableview.mediaTableView
import org.bibletranslationtools.maui.jvm.ui.events.AppSaveRequestEvent
import org.bibletranslationtools.maui.jvm.ui.BatchDataStore
import org.bibletranslationtools.maui.jvm.ui.UploadTarget
import org.bibletranslationtools.maui.jvm.ui.components.mainHeader
import org.bibletranslationtools.maui.jvm.ui.components.uploadTargetHeader
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import tornadofx.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
                addClass("root-container")

                vgrow = Priority.ALWAYS

                hbox {
                    addClass("controls")

                    vbox {
                        spacing = 4.0

                        label {
                            addClass("controls-title")
                            textProperty().bind(batchDataStore.activeBatchProperty.stringBinding { it?.name })
                        }
                        label {
                            addClass("controls-subtitle")
                            textProperty().bind(batchDataStore.activeBatchProperty.stringBinding {
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
                        addClass("btn", "btn--secondary")
                        graphic = FontIcon(MaterialDesign.MDI_CONTENT_SAVE)

                        action {
                            println("Save project")
                            viewModel.sortedMediaItems.forEach {
                                println(it.file.name)
                                println(it.language)
                                println(it.resourceType)
                                println(it.book)
                                println(it.chapter)
                                println(it.mediaExtension)
                                println(it.mediaQuality)
                                println(it.grouping)
                                println(it.status)
                                println("-------------------------")
                            }
                        }
                    }

                    button(messages["viewUploadedFiles"]) {
                        addClass("btn", "btn--secondary")
                        graphic = FontIcon(MaterialDesign.MDI_EXPORT)

                        action {
                            println("View uploaded files")
                        }
                    }

                    button(messages["exportCsv"]) {
                        addClass("btn", "btn--secondary")
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

                    languagesProperty.set(viewModel.languages)
                    resourceTypesProperty.set(viewModel.resourceTypes)
                    booksProperty.set(viewModel.books)
                    mediaExtensionsProperty.set(viewModel.mediaExtensions)
                    mediaQualitiesProperty.set(viewModel.mediaQualities)
                    groupingsProperty.set(viewModel.groupings)
                    statusesProperty.set(viewModel.statuses)

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