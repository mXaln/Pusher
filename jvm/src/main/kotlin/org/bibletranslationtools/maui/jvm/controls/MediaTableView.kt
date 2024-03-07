package org.bibletranslationtools.maui.jvm.controls

import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.event.EventTarget
import javafx.scene.Node
import javafx.scene.control.TableView
import javafx.scene.layout.Priority
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
    val groupingColumnProperty = SimpleStringProperty()
    val statusColumnProperty = SimpleStringProperty()

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
            minWidth = 48.0
            maxWidth = 48.0

            setCellValueFactory {
                checkbox { }.toProperty()
            }
        }

        column("", String::class) {
            addClass("file-column")

            textProperty().bind(fileNameColumnProperty)
            setCellValueFactory {
                it.value.file.name.toProperty()
            }
            isReorderable = false
        }

        column("", String::class) {
            textProperty().bind(languageColumnProperty)
            setCellValueFactory {
                it.value.languageProperty
            }
            isReorderable = false
        }

        column("", String::class) {
            textProperty().bind(resourceTypeColumnProperty)
            setCellValueFactory {
                it.value.resourceTypeProperty
            }
            isReorderable = false
        }

        column("", String::class) {
            textProperty().bind(bookColumnProperty)
            setCellValueFactory {
                it.value.bookProperty
            }
            isReorderable = false
        }

        column("", String::class) {
            textProperty().bind(chapterColumnProperty)
            setCellValueFactory {
                it.value.chapterProperty
            }
            isReorderable = false
        }

        column("", String::class) {
            textProperty().bind(groupingColumnProperty)
            setCellValueFactory {
                it.value.groupingProperty.stringBinding { grouping -> grouping?.grouping }
            }
            isReorderable = false
        }

        column("", String::class) {
            textProperty().bind(statusColumnProperty)
            setCellValueFactory {
                it.value.statusProperty.stringBinding { status -> status?.name }
            }
            isReorderable = false
        }
    }
}

fun EventTarget.mediaTableView(
    values: ObservableList<MediaItem>,
    op: MediaTableView.() -> Unit = {}
) = MediaTableView(values).attachTo(this, op)