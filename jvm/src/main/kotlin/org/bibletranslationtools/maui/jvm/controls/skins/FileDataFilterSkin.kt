package org.bibletranslationtools.maui.jvm.controls.skins

import javafx.beans.property.Property
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.SkinBase
import javafx.scene.control.TextField
import org.bibletranslationtools.maui.common.data.Grouping
import org.bibletranslationtools.maui.common.data.MediaExtension
import org.bibletranslationtools.maui.common.data.MediaQuality
import org.bibletranslationtools.maui.jvm.controls.filedatafilter.FileDataFilter
import org.bibletranslationtools.maui.jvm.controls.filedatafilter.MAX_CHAPTER_LENGTH
import tornadofx.*

class FileDataFilterSkin(private val filter: FileDataFilter) : SkinBase<FileDataFilter>(filter) {

    @FXML
    private lateinit var languageLabel: Label

    @FXML
    private lateinit var languagesList: ComboBox<String>

    @FXML
    private lateinit var resourceTypeLabel: Label

    @FXML
    private lateinit var resourceTypesList: ComboBox<String>

    @FXML
    private lateinit var bookLabel: Label

    @FXML
    private lateinit var booksList: ComboBox<String>

    @FXML
    private lateinit var chapterLabel: Label

    @FXML
    private lateinit var chapter: TextField

    @FXML
    private lateinit var mediaExtensionLabel: Label

    @FXML
    private lateinit var mediaExtensionsList: ComboBox<MediaExtension>

    @FXML
    private lateinit var mediaQualityLabel: Label

    @FXML
    private lateinit var mediaQualitiesList: ComboBox<MediaQuality>

    @FXML
    private lateinit var groupingLabel: Label

    @FXML
    private lateinit var groupingsList: ComboBox<Grouping>

    private var isSelectionChanging = false

    init {
        loadFXML()
        initialize()
    }

    private fun initialize() {
        initializeControls()
        initializeActions()
        bindFilterSelections()
    }

    private fun initializeControls() {
        languageLabel.textProperty().bind(filter.languageLabelProperty)
        languagesList.itemsProperty().bind(filter.languagesProperty)

        resourceTypeLabel.textProperty().bind(filter.resourceTypeLabelProperty)
        resourceTypesList.itemsProperty().bind(filter.resourceTypesProperty)

        bookLabel.textProperty().bind(filter.bookLabelProperty)
        booksList.itemsProperty().bind(filter.booksProperty)

        chapterLabel.textProperty().bind(filter.chapterLabelProperty)
        chapter.filterInput {
            it.controlNewText.isInt() && it.controlNewText.length <= MAX_CHAPTER_LENGTH
        }

        mediaExtensionLabel.textProperty().bind(filter.mediaExtensionLabelProperty)
        mediaExtensionsList.itemsProperty().bind(filter.mediaExtensionsProperty)

        mediaQualityLabel.textProperty().bind(filter.mediaQualityLabelProperty)
        mediaQualitiesList.itemsProperty().bind(filter.mediaQualitiesProperty)

        groupingLabel.textProperty().bind(filter.groupingLabelProperty)
        groupingsList.itemsProperty().bind(filter.groupingsProperty)
    }

    private fun initializeActions() {
        languagesList.selectionModel.selectedItemProperty().addListener { _, old, new ->
            if (new != null) {
                itemSelectionAction(filter.selectedLanguageProperty, languagesList, old, new)
            }
        }

        resourceTypesList.selectionModel.selectedItemProperty().addListener { _, old, new ->
            if (new != null) {
                itemSelectionAction(filter.selectedResourceTypeProperty, resourceTypesList, old, new)
            }
        }

        booksList.selectionModel.selectedItemProperty().addListener { _, old, new ->
            if (new != null) {
                itemSelectionAction(filter.selectedBookProperty, booksList, old, new)
            }
        }

        chapter.setOnAction {
            filter.onConfirmActionProperty.value.handle(ActionEvent())
            filter.callbackObserver?.subscribe { answer ->
                if (answer) {
                    filter.chapterProperty.value = chapter.text
                } else {
                    chapter.text = null
                    filter.chapterProperty.value = null
                }
            }
        }

        mediaExtensionsList.selectionModel.selectedItemProperty().addListener { _, old, new ->
            if (new != null) {
                itemSelectionAction(filter.selectedMediaExtensionProperty, mediaExtensionsList, old, new)
            }
        }

        mediaQualitiesList.selectionModel.selectedItemProperty().addListener { _, old, new ->
            if (new != null) {
                itemSelectionAction(filter.selectedMediaQualityProperty, mediaQualitiesList, old, new)
            }
        }

        groupingsList.selectionModel.selectedItemProperty().addListener { _, old, new ->
            if (new != null) {
                itemSelectionAction(filter.selectedGroupingProperty, groupingsList, old, new)
            }
        }
    }

    private fun bindFilterSelections() {
        filter.selectedLanguageProperty.onChange {
            languagesList.selectionModel.select(it)
        }

        filter.selectedResourceTypeProperty.onChange {
            resourceTypesList.selectionModel.select(it)
        }

        filter.selectedBookProperty.onChange {
            booksList.selectionModel.select(it)
        }

        filter.chapterProperty.onChange {
            chapter.text = it
        }

        filter.selectedMediaExtensionProperty.onChange {
            mediaExtensionsList.selectionModel.select(it)
        }

        filter.selectedMediaQualityProperty.onChange {
            mediaQualitiesList.selectionModel.select(it)
        }

        filter.selectedGroupingProperty.onChange {
            groupingsList.selectionModel.select(it)
        }
    }

    private fun <T> itemSelectionAction(selected: Property<T?>, dropdownList: ComboBox<T>, old: T?, new: T?) {
        if (!isSelectionChanging) {
            isSelectionChanging = true
            if (dropdownList.value in dropdownList.items) {
                filter.onConfirmActionProperty.value.handle(ActionEvent())
                filter.callbackObserver?.subscribe { answer ->
                    if (answer) {
                        selected.value = new
                        isSelectionChanging = false
                    } else {
                        runLater {
                            dropdownList.selectionModel.select(old)
                            selected.value = old
                            isSelectionChanging = false
                        }
                    }
                }
            } else {
                runLater {
                    dropdownList.selectionModel.select(old)
                    selected.value = old
                    isSelectionChanging = false
                }
            }
        }
    }

    private fun loadFXML() {
        val loader = FXMLLoader(javaClass.getResource("/skins/FileDataFilter.fxml"))
        loader.setController(this)
        val root: Node = loader.load()
        children.add(root)
    }
}
