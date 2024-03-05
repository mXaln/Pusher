package org.bibletranslationtools.maui.jvm.controls

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.event.EventTarget
import javafx.scene.Node
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

    val noBatchesPromptProperty = SimpleStringProperty()
    val batchNameLabelProperty = SimpleStringProperty()
    val batchDateLabelProperty = SimpleStringProperty()
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
                label(noBatchesPromptProperty) {
                    addClass("batch-table-view__placeholder-text")
                }
            }
        }

        column("", Node::class) {
            textProperty().bind(batchNameLabelProperty)
            setCellValueFactory {
                label(it.value.name) {
                    addClass("batch-table-view__name")
                    graphic = FontIcon(MaterialDesign.MDI_FILE)
                }.toProperty()
            }
            isResizable = false
            isReorderable = false
            prefWidthProperty().bind(this@BatchTableView.widthProperty().multiply(0.45))
        }
        column("", String::class) {
            textProperty().bind(batchDateLabelProperty)
            setCellValueFactory {
                val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss a")
                it.value.created.format(formatter).toProperty()
            }
            isResizable = false
            isReorderable = false
            prefWidthProperty().bind(this@BatchTableView.widthProperty().multiply(0.45))
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
            isResizable = false
            isReorderable = false
            prefWidthProperty().bind(this@BatchTableView.widthProperty().multiply(0.1))
        }
    }
}

fun EventTarget.batchTableView(
    values: ObservableList<Batch>,
    op: BatchTableView.() -> Unit = {}
) = BatchTableView(values).attachTo(this, op)
