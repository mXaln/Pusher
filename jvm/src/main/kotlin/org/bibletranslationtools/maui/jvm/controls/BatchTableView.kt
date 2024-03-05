package org.bibletranslationtools.maui.jvm.controls

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.event.EventTarget
import javafx.scene.Node
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.layout.Priority
import org.bibletranslationtools.maui.common.data.Batch
import org.bibletranslationtools.maui.jvm.assets.AppResources
import org.bibletranslationtools.maui.jvm.onChangeAndDoNow
import org.bibletranslationtools.maui.jvm.ui.UploadTarget
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import tornadofx.*
import java.time.format.DateTimeFormatter

class BatchTableView(
    batches: ObservableList<Batch>
) : TableView<Batch>(batches) {

    val emptyPromptProperty = SimpleStringProperty()
    val nameColumnProperty = SimpleStringProperty()
    val dateColumnProperty = SimpleStringProperty()
    val uploadTargetProperty = SimpleObjectProperty<UploadTarget>()
    val deleteTextProperty = SimpleStringProperty()

    init {
        addClass("batch-table-view")
        importStylesheet(AppResources.load("/css/batch-table-view.css"))

        uploadTargetProperty.onChangeAndDoNow {
            togglePseudoClass("accent", it == UploadTarget.DEV)
        }

        vgrow = Priority.ALWAYS
        columnResizePolicy = CONSTRAINED_RESIZE_POLICY

        placeholder = borderpane {
            center = vbox {
                addClass("batch-table-view__placeholder")

                label {
                    addClass("batch-table-view__placeholder-icon")
                    graphic = FontIcon(MaterialDesign.MDI_FOLDER_OUTLINE)
                }
                label(emptyPromptProperty) {
                    addClass("batch-table-view__placeholder-text")
                }
            }
        }

        column("", Node::class) {
            textProperty().bind(nameColumnProperty)
            setCellValueFactory {
                label(it.value.name) {
                    addClass("batch-table-view__name")
                    graphic = FontIcon(MaterialDesign.MDI_FILE)
                }.toProperty()
            }
            bindVisibleWhenNotEmpty()
            isReorderable = false
        }
        column("", String::class) {
            textProperty().bind(dateColumnProperty)
            setCellValueFactory {
                val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss a")
                it.value.created.format(formatter).toProperty()
            }
            bindVisibleWhenNotEmpty()
            isReorderable = false
        }
        column("", Node::class) {
            setCellValueFactory {
                hbox {
                    addClass("actions-column__buttons")
                    button {
                        addClass("btn", "btn--icon", "btn--delete")

                        graphic = FontIcon(MaterialDesign.MDI_DELETE)
                        tooltip {
                            textProperty().bind(deleteTextProperty)
                        }

                        action {
                            println("delete")
                        }
                    }
                }.toProperty()
            }
            isReorderable = false
        }
    }

    private fun <S, T> TableColumn<S, T>.bindVisibleWhenNotEmpty() {
        visibleProperty().bind(itemsProperty().booleanBinding {
            it?.isNotEmpty() ?: false
        })
    }
}

fun EventTarget.batchTableView(
    values: ObservableList<Batch>,
    op: BatchTableView.() -> Unit = {}
) = BatchTableView(values).attachTo(this, op)
