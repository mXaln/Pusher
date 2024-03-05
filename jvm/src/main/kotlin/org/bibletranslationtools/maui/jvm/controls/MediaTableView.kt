package org.bibletranslationtools.maui.jvm.controls

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.event.EventTarget
import javafx.scene.Node
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.layout.Priority
import org.bibletranslationtools.maui.jvm.assets.AppResources
import org.bibletranslationtools.maui.jvm.onChangeAndDoNow
import org.bibletranslationtools.maui.jvm.ui.MediaItem
import org.bibletranslationtools.maui.jvm.ui.UploadTarget
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import tornadofx.*

class MediaTableView(
    media: ObservableList<MediaItem>
) : TableView<MediaItem>(media) {

    val emptyPromptProperty = SimpleStringProperty()
    val fileNameColumnProperty = SimpleStringProperty()
    val languageColumnProperty = SimpleStringProperty()
    val resourceTypeColumnProperty = SimpleStringProperty()
    val bookColumnProperty = SimpleStringProperty()
    val chapterColumnProperty = SimpleStringProperty()
    val groupingColumnProperty = SimpleStringProperty()
    val statusColumnProperty = SimpleStringProperty()
    val uploadTargetProperty = SimpleObjectProperty<UploadTarget>()

    init {
        addClass("media-table-view")
        importStylesheet(AppResources.load("/css/media-table-view.css"))

        uploadTargetProperty.onChangeAndDoNow {
            togglePseudoClass("accent", it == UploadTarget.DEV)
        }

        vgrow = Priority.ALWAYS
        columnResizePolicy = CONSTRAINED_RESIZE_POLICY

        placeholder = borderpane {
            center = vbox {
                addClass("media-table-view__placeholder")

                label {
                    addClass("media-table-view__placeholder-icon")
                    graphic = FontIcon(MaterialDesign.MDI_FILE_OUTLINE)
                }
                label(emptyPromptProperty) {
                    addClass("media-table-view__placeholder-text")
                }
            }
        }

        column("", Node::class) {
            setCellValueFactory {
                checkbox {  }.toProperty()
            }
            bindVisibleWhenNotEmpty()
        }

        column("", String::class) {
            textProperty().bind(fileNameColumnProperty)
            setCellValueFactory {
                it.value.file.name.toProperty()
            }
            bindVisibleWhenNotEmpty()
            isReorderable = false
        }

        column("", String::class) {
            textProperty().bind(languageColumnProperty)
            setCellValueFactory {
                it.value.languageProperty
            }
            bindVisibleWhenNotEmpty()
            isReorderable = false
        }

        column("", String::class) {
            textProperty().bind(resourceTypeColumnProperty)
            setCellValueFactory {
                it.value.resourceTypeProperty
            }
            bindVisibleWhenNotEmpty()
            isReorderable = false
        }

        column("", String::class) {
            textProperty().bind(bookColumnProperty)
            setCellValueFactory {
                it.value.bookProperty
            }
            bindVisibleWhenNotEmpty()
            isReorderable = false
        }

        column("", String::class) {
            textProperty().bind(chapterColumnProperty)
            setCellValueFactory {
                it.value.chapterProperty
            }
            bindVisibleWhenNotEmpty()
            isReorderable = false
        }

        column("", String::class) {
            textProperty().bind(groupingColumnProperty)
            setCellValueFactory {
                it.value.groupingProperty.stringBinding { grouping -> grouping?.grouping }
            }
            bindVisibleWhenNotEmpty()
            isReorderable = false
        }

        column("", String::class) {
            textProperty().bind(statusColumnProperty)
            setCellValueFactory {
                "Status".toProperty()
            }
            bindVisibleWhenNotEmpty()
            isReorderable = false
        }
    }

    private fun <S, T> TableColumn<S, T>.bindVisibleWhenNotEmpty() {
        visibleProperty().bind(itemsProperty().booleanBinding {
            it?.isNotEmpty() ?: false
        })
    }
}

fun EventTarget.mediaTableView(
    values: ObservableList<MediaItem>,
    op: MediaTableView.() -> Unit = {}
) = MediaTableView(values).attachTo(this, op)