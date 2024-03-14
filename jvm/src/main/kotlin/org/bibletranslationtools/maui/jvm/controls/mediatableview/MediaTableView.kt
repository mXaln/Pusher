package org.bibletranslationtools.maui.jvm.controls.mediatableview

import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.event.EventTarget
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.layout.Priority
import org.bibletranslationtools.maui.common.data.FileStatus
import org.bibletranslationtools.maui.common.data.Grouping
import org.bibletranslationtools.maui.common.data.MediaExtension
import org.bibletranslationtools.maui.common.data.MediaQuality
import org.bibletranslationtools.maui.jvm.assets.AppResources
import org.bibletranslationtools.maui.jvm.customizeScrollbarSkin
import org.bibletranslationtools.maui.jvm.ui.MediaItem
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import tornadofx.*

class MediaTableView(
    val media: ObservableList<MediaItem>
) : TableView<MediaItem>(media) {

    val emptyPromptProperty = SimpleStringProperty()
    val fileNameColumnProperty = SimpleStringProperty()
    val languageColumnProperty = SimpleStringProperty()
    val resourceTypeColumnProperty = SimpleStringProperty()
    val bookColumnProperty = SimpleStringProperty()
    val chapterColumnProperty = SimpleStringProperty()
    val mediaExtensionColumnProperty = SimpleStringProperty()
    val mediaQualityColumnProperty = SimpleStringProperty()
    val groupingColumnProperty = SimpleStringProperty()
    val statusColumnProperty = SimpleStringProperty()

    val languagesProperty = SimpleListProperty<String>()
    val resourceTypesProperty = SimpleListProperty<String>()
    val booksProperty = SimpleListProperty<String>()
    val mediaExtensionsProperty = SimpleListProperty<MediaExtension>()
    val mediaQualitiesProperty = SimpleListProperty<MediaQuality>()
    val groupingsProperty = SimpleListProperty<Grouping>()
    val statusesProperty = SimpleListProperty<FileStatus>()

    init {
        addClass("media-table-view")
        importStylesheet(AppResources.load("/css/media-table-view.css"))

        runLater { customizeScrollbarSkin() }

        vgrow = Priority.ALWAYS
        columnResizePolicy = CONSTRAINED_RESIZE_POLICY

        placeholder = borderpane {
            center = vbox {
                addClass("placeholder")

                label {
                    addClass("placeholder-icon")
                    graphic = FontIcon(MaterialDesign.MDI_FILE_OUTLINE)
                }
                label(emptyPromptProperty) {
                    addClass("placeholder-text")
                }
            }
        }

        column("", Node::class) {
            setCellValueFactory {
                checkbox {

                }.toProperty()
            }

            graphic = checkbox {
                action { println("Hi!") }
            }

            minWidth = 40.0
            bindColumnWidth(3.0)

            isSortable = false
            isReorderable = true
        }

        column("", String::class) {
            addClass("file-column")

            textProperty().bind(fileNameColumnProperty)

            setCellValueFactory {
                it.value.file.name.toProperty()
            }

            bindColumnWidth(28.7)
            isReorderable = false
        }

        column("", String::class) {
            textProperty().bind(languageColumnProperty)

            setCellValueFactory {
                it.value.languageProperty
            }

            setCellFactory {
                OptionTableCell<String>(languagesProperty).apply {
                    setOnOptionChanged {
                        rowItem.language = it
                    }
                }
            }

            bindColumnWidth(9.0)
            isSortable = false
            isReorderable = false
        }

        column("", String::class) {
            textProperty().bind(resourceTypeColumnProperty)

            setCellValueFactory {
                it.value.resourceTypeProperty
            }

            setCellFactory {
                OptionTableCell<String>(resourceTypesProperty).apply {
                    setOnOptionChanged {
                        rowItem.resourceType = it
                    }
                }
            }

            bindColumnWidth(9.0)
            isSortable = false
            isReorderable = false
        }

        column("", String::class) {
            textProperty().bind(bookColumnProperty)

            setCellValueFactory {
                it.value.bookProperty
            }

            setCellFactory {
                OptionTableCell<String>(booksProperty).apply {
                    setOnOptionChanged {
                        rowItem.book = it
                    }
                }
            }

            minWidth = 70.0
            bindColumnWidth(6.0)
            isSortable = false
            isReorderable = false
        }

        column("", String::class) {
            textProperty().bind(chapterColumnProperty)

            setCellValueFactory {
                it.value.chapterProperty
            }

            setCellFactory {
                TextTableCell().apply {
                    setOnTextChanged {
                        rowItem.chapter = it
                    }
                }
            }

            bindColumnWidth(6.0)
            isSortable = false
            isReorderable = false
        }

        column("", MediaExtension::class) {
            textProperty().bind(mediaExtensionColumnProperty)

            setCellValueFactory {
                it.value.mediaExtensionProperty
            }

            setCellFactory {
                OptionTableCell<MediaExtension>(mediaExtensionsProperty, false).apply {
                    setOnOptionChanged {
                        rowItem.mediaExtension = it
                    }
                }
            }

            bindColumnWidth(10.0)
            isSortable = false
            isReorderable = false
        }

        column("", MediaQuality::class) {
            textProperty().bind(mediaQualityColumnProperty)

            setCellValueFactory {
                it.value.mediaQualityProperty
            }

            setCellFactory {
                OptionTableCell<MediaQuality>(mediaQualitiesProperty, false).apply {
                    setOnOptionChanged {
                        rowItem.mediaQuality = it
                    }
                }
            }

            bindColumnWidth(9.0)
            isSortable = false
            isReorderable = false
        }

        column("", Grouping::class) {
            textProperty().bind(groupingColumnProperty)

            setCellValueFactory {
                it.value.groupingProperty
            }

            setCellFactory {
                OptionTableCell<Grouping>(groupingsProperty, false).apply {
                    setOnOptionChanged {
                        rowItem.grouping = it
                    }
                }
            }

            bindColumnWidth(9.0)
            isSortable = false
            isReorderable = false
        }

        column("", Node::class) {
            textProperty().bind(statusColumnProperty)

            setCellValueFactory { item ->
                statusColumnView(item.value).toProperty()
            }

            bindColumnWidth(10.0)
            isSortable = false
            isReorderable = false
        }
    }
}

internal fun <S, T> TableColumn<S, T>.bindColumnWidth(percent: Double) {
    isResizable = false
    prefWidthProperty().bind(tableView.widthProperty().multiply(percent / 100.0))
}

fun EventTarget.mediaTableView(
    values: ObservableList<MediaItem>,
    op: MediaTableView.() -> Unit = {}
) = MediaTableView(values).attachTo(this, op)