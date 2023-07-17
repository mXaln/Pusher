package org.bibletranslationtools.maui.jvm.ui.filedatacell

import com.jfoenix.controls.JFXComboBox
import com.jfoenix.controls.JFXTextField
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.ListCell
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.bibletranslationtools.maui.jvm.assets.AppResources
import org.bibletranslationtools.maui.common.data.Grouping
import org.bibletranslationtools.maui.jvm.controls.filedatafilter.MAX_CHAPTER_LENGTH
import org.bibletranslationtools.maui.jvm.ui.FileDataItem
import org.bibletranslationtools.maui.jvm.ui.main.MainViewModel
import tornadofx.*

class FileDataView : VBox() {
    val fileDataItemProperty = SimpleObjectProperty<FileDataItem>(null)
    val fileDataItem: FileDataItem? by fileDataItemProperty

    private val mainViewModel = find<MainViewModel>()

    init {
        importStylesheet(AppResources.load("/css/file-data-cell.css"))
        addClass("file-data-cell")

        hbox {
            label().apply {
                fileDataItemProperty.onChange {
                    it?.let {
                        text = it.file.name
                    }
                }
            }
            addClass("file-data-cell__title")
        }
        hbox {
            addClass("file-data-cell__options")
            spacing = 10.0
            vbox {
                hgrow = Priority.ALWAYS
                label(FX.messages["language"])
                add(
                    JFXComboBox(mainViewModel.languages).apply {
                        addClass("file-data-cell__dropdown")

                        isEditable = true

                        fileDataItemProperty.onChange {
                            it?.let {
                                selectionModel.select(it.language)
                            }
                        }
                        setOnAction {
                            if(selectedItem in items) {
                                fileDataItem?.language = selectedItem
                            } else {
                                if (selectedItem != null) {
                                    FX.eventbus.fire(ErrorOccurredEvent("Language $selectedItem not found"))
                                    selectionModel.select(fileDataItem?.language)
                                }
                            }
                        }
                    }
                )
            }
            vbox {
                hgrow = Priority.ALWAYS
                label(FX.messages["resourceType"])
                add(
                    JFXComboBox(mainViewModel.resourceTypes).apply {
                        addClass("file-data-cell__dropdown")

                        isEditable = true

                        fileDataItemProperty.onChange {
                            it?.let {
                                selectionModel.select(it.resourceType)
                            }
                        }
                        setOnAction {
                            if(selectedItem in items) {
                                fileDataItem?.resourceType = selectedItem
                            } else {
                                if (selectedItem != null) {
                                    FX.eventbus.fire(ErrorOccurredEvent("Resource Type $selectedItem not found"))
                                    selectionModel.select(fileDataItem?.resourceType)
                                }
                            }
                        }
                    }
                )
            }
            vbox {
                hgrow = Priority.ALWAYS
                label(FX.messages["book"])
                add(
                    JFXComboBox(mainViewModel.books).apply {
                        addClass("file-data-cell__dropdown")

                        isEditable = true

                        fileDataItemProperty.onChange {
                            it?.let {
                                selectionModel.select(it.book)
                            }
                        }
                        setOnAction {
                            if(selectedItem in items) {
                                fileDataItem?.book = selectedItem
                            } else {
                                if (selectedItem != null) {
                                    FX.eventbus.fire(ErrorOccurredEvent("Book $selectedItem not found"))
                                    selectionModel.select(fileDataItem?.book)
                                }
                            }
                        }
                    }
                )
            }
            vbox {
                hgrow = Priority.ALWAYS
                label(FX.messages["chapter"])
                add(
                    JFXTextField().apply {
                        addClass("file-data-cell__chapter")

                        isEditable = true

                        fileDataItemProperty.onChange {
                            it?.let {
                                text = it.chapter.toString()
                            }
                        }

                        textProperty().onChange {
                            fileDataItem?.chapter = it
                        }

                        filterInput {
                            it.controlNewText.isInt() && it.controlNewText.length <= MAX_CHAPTER_LENGTH
                        }
                    }
                )
            }
            vbox {
                hgrow = Priority.ALWAYS
                label(FX.messages["mediaExtension"])
                add(
                    JFXComboBox(mainViewModel.mediaExtensions).apply {
                        addClass("file-data-cell__dropdown")

                        fileDataItemProperty.onChange {
                            it?.let {
                                enableWhen(it.mediaExtensionAvailable)
                                selectionModel.select(it.mediaExtension)
                            }
                        }
                        selectionModel.selectedItemProperty().onChange {
                            fileDataItem?.mediaExtension = it
                        }
                    }
                )
            }
            vbox {
                hgrow = Priority.ALWAYS
                label(FX.messages["mediaQuality"])
                add(
                    JFXComboBox(mainViewModel.mediaQualities).apply {
                        addClass("file-data-cell__dropdown")

                        fileDataItemProperty.onChange {
                            it?.let {
                                selectionModel.select(it.mediaQuality)
                                enableWhen(it.mediaQualityAvailable)
                            }
                        }
                        selectionModel.selectedItemProperty().onChange {
                            fileDataItem?.mediaQuality = it
                        }
                    }
                )
            }
            vbox {
                hgrow = Priority.ALWAYS
                label(FX.messages["grouping"])
                add(
                    JFXComboBox(mainViewModel.groupings).apply {
                        addClass("file-data-cell__dropdown")

                        fileDataItemProperty.onChange {
                            it?.let {
                                selectionModel.select(it.grouping)
                                isDisable = it.grouping != null
                            }
                        }
                        selectionModel.selectedItemProperty().onChange {
                            fileDataItem?.grouping = it
                        }

                        setCellFactory {
                            object : ListCell<Grouping>() {
                                override fun updateItem(item: Grouping?, empty: Boolean) {
                                    super.updateItem(item, empty)
                                    text = item?.toString() ?: ""

                                    fileDataItem?.let {
                                        if (mainViewModel.restrictedGroupings(it).contains(item)) {
                                            isDisable = true
                                            opacity = 0.5
                                        }
                                    }
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

class ErrorOccurredEvent(val message:String): FXEvent()