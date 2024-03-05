package org.bibletranslationtools.maui.jvm.ui.mediacell

import com.jfoenix.controls.JFXComboBox
import com.jfoenix.controls.JFXTextField
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.ListCell
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.bibletranslationtools.maui.jvm.assets.AppResources
import org.bibletranslationtools.maui.common.data.Grouping
import org.bibletranslationtools.maui.jvm.controls.mediafilter.MAX_CHAPTER_LENGTH
import org.bibletranslationtools.maui.jvm.ui.MediaItem
import org.bibletranslationtools.maui.jvm.ui.main.MainViewModel
import tornadofx.*

class MediaView : VBox() {
    val mediaItemProperty = SimpleObjectProperty<MediaItem>(null)
    val mediaItem: MediaItem? by mediaItemProperty

    private val mainViewModel = find<MainViewModel>()

    init {
        importStylesheet(AppResources.load("/css/media-cell.css"))
        addClass("media-cell")

        hbox {
            label().apply {
                mediaItemProperty.onChange {
                    it?.let {
                        text = it.file.name
                    }
                }
            }
            addClass("media-cell__title")
        }
        hbox {
            addClass("media-cell__options")
            spacing = 10.0
            vbox {
                hgrow = Priority.ALWAYS
                label(FX.messages["language"])
                add(
                    JFXComboBox(mainViewModel.languages).apply {
                        addClass("media-cell__dropdown")

                        isEditable = true

                        mediaItemProperty.onChange {
                            it?.let {
                                selectionModel.select(it.language)
                            }
                        }
                        setOnAction {
                            if(selectedItem in items) {
                                mediaItem?.language = selectedItem
                            } else {
                                if (selectedItem != null) {
                                    FX.eventbus.fire(ErrorOccurredEvent("Language $selectedItem not found"))
                                    selectionModel.select(mediaItem?.language)
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
                        addClass("media-cell__dropdown")

                        isEditable = true

                        mediaItemProperty.onChange {
                            it?.let {
                                selectionModel.select(it.resourceType)
                            }
                        }
                        setOnAction {
                            if(selectedItem in items) {
                                mediaItem?.resourceType = selectedItem
                            } else {
                                if (selectedItem != null) {
                                    FX.eventbus.fire(ErrorOccurredEvent("Resource Type $selectedItem not found"))
                                    selectionModel.select(mediaItem?.resourceType)
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
                        addClass("media-cell__dropdown")

                        isEditable = true

                        mediaItemProperty.onChange {
                            it?.let {
                                selectionModel.select(it.book)
                            }
                        }
                        setOnAction {
                            if(selectedItem in items) {
                                mediaItem?.book = selectedItem
                            } else {
                                if (selectedItem != null) {
                                    FX.eventbus.fire(ErrorOccurredEvent("Book $selectedItem not found"))
                                    selectionModel.select(mediaItem?.book)
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
                        addClass("media-cell__chapter")

                        isEditable = true

                        mediaItemProperty.onChange {
                            it?.let {
                                text = it.chapter.toString()
                            }
                        }

                        textProperty().onChange {
                            mediaItem?.chapter = it
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
                        addClass("media-cell__dropdown")

                        mediaItemProperty.onChange {
                            it?.let {
                                enableWhen(it.mediaExtensionAvailable)
                                selectionModel.select(it.mediaExtension)
                            }
                        }
                        selectionModel.selectedItemProperty().onChange {
                            mediaItem?.mediaExtension = it
                        }
                    }
                )
            }
            vbox {
                hgrow = Priority.ALWAYS
                label(FX.messages["mediaQuality"])
                add(
                    JFXComboBox(mainViewModel.mediaQualities).apply {
                        addClass("media-cell__dropdown")

                        mediaItemProperty.onChange {
                            it?.let {
                                selectionModel.select(it.mediaQuality)
                                enableWhen(it.mediaQualityAvailable)
                            }
                        }
                        selectionModel.selectedItemProperty().onChange {
                            mediaItem?.mediaQuality = it
                        }
                    }
                )
            }
            vbox {
                hgrow = Priority.ALWAYS
                label(FX.messages["grouping"])
                add(
                    JFXComboBox(mainViewModel.groupings).apply {
                        addClass("media-cell__dropdown")

                        mediaItemProperty.onChange {
                            it?.let {
                                selectionModel.select(it.grouping)
                                isDisable = it.grouping != null
                            }
                        }
                        selectionModel.selectedItemProperty().onChange {
                            mediaItem?.grouping = it
                        }

                        setCellFactory {
                            object : ListCell<Grouping>() {
                                override fun updateItem(item: Grouping?, empty: Boolean) {
                                    super.updateItem(item, empty)
                                    text = item?.toString() ?: ""

                                    mediaItem?.let {
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