package org.bibletranslationtools.maui.jvm.ui.main

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.stage.DirectoryChooser
import org.bibletranslationtools.maui.common.audio.BttrChunk
import org.bibletranslationtools.maui.common.data.MediaExtension
import org.bibletranslationtools.maui.common.data.MediaQuality
import org.bibletranslationtools.maui.common.data.Grouping
import org.bibletranslationtools.maui.common.io.Versification
import org.bibletranslationtools.maui.common.usecases.FileProcessingRouter
import org.bibletranslationtools.maui.common.usecases.FileVerifier
import org.bibletranslationtools.maui.common.usecases.MakePath
import org.bibletranslationtools.maui.common.usecases.TransferFile
import org.bibletranslationtools.maui.jvm.client.FtpTransferClient
import org.bibletranslationtools.maui.jvm.io.BooksReader
import org.bibletranslationtools.maui.jvm.io.HtmlWriter
import org.bibletranslationtools.maui.jvm.io.LanguagesReader
import org.bibletranslationtools.maui.jvm.io.VersificationReader
import org.bibletranslationtools.maui.jvm.ui.MediaItem
import org.bibletranslationtools.maui.jvm.mappers.MediaMapper
import org.bibletranslationtools.maui.jvm.mappers.VerifiedResultMapper
import org.thymeleaf.TemplateEngine
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import org.bibletranslationtools.maui.jvm.io.ResourceTypesReader
import org.bibletranslationtools.maui.jvm.ui.mediacell.ErrorOccurredEvent
import org.wycliffeassociates.otter.common.audio.wav.WavFile
import org.wycliffeassociates.otter.common.audio.wav.WavMetadata
import tornadofx.*
import java.io.File
import java.text.MessageFormat
import java.util.regex.Pattern
import io.reactivex.rxkotlin.toObservable as toRxObservable

class MainViewModel : ViewModel() {
    val mediaItems = observableListOf<MediaItem>()
    val mediaItemsProperty = SimpleListProperty(mediaItems)
    val successfulUploadProperty = SimpleBooleanProperty(false)

    val languages = observableListOf<String>()
    val resourceTypes = observableListOf<String>()
    val books = observableListOf<String>()
    val mediaExtensions = MediaExtension.values().toList().toObservable()
    val mediaQualities = MediaQuality.values().toList().toObservable()
    val groupings = Grouping.values().toList().toObservable()
    private val versification = SimpleObjectProperty<Versification>()

    val isProcessing = SimpleBooleanProperty(false)
    val snackBarObservable: PublishSubject<String> = PublishSubject.create()
    val updatedObservable: PublishSubject<Boolean> = PublishSubject.create()

    //private val fileProcessRouter = FileProcessingRouter.build()
    private lateinit var fileVerifier: FileVerifier
    private val verifiedResultMapper = VerifiedResultMapper()
    private val mediaMapper = MediaMapper()
    private val thymeleafEngine = TemplateEngine()
    private val htmlWriter = HtmlWriter()

    init {
        initThymeleafEngine()
        loadLanguages()
        loadResourceTypes()
        loadBooks()
        loadVersification()

        subscribe<ErrorOccurredEvent> {
            snackBarObservable.onNext(it.message)
        }
    }

    fun onDropFiles(files: List<File>) {
        isProcessing.set(true)
        val filesToImport = prepareFilesToImport(files)
        importFiles(filesToImport)
    }

    fun verify() {
        val directoryChooser = DirectoryChooser()
        val file = directoryChooser.showDialog(primaryStage)
        val filename = "${file.absolutePath}/report.html"

        isProcessing.set(true)
        mediaItems.toRxObservable()
            .map { mediaItem ->
                fileVerifier.handleItem(mediaMapper.toEntity(mediaItem))
            }
            .toList()
            .map { results ->
                verifiedResultMapper.fromEntity(results)
            }
            .map { context ->
                thymeleafEngine.process("VerifiedResultsTemplate", context)
            }
            .observeOnFx()
            .doFinally { isProcessing.set(false) }
            .subscribe { html ->
                htmlWriter.write(filename, html)
                snackBarObservable.onNext("Finished verifying files. Reported into file $filename")
            }
    }

    fun upload() {
        isProcessing.set(true)
        mediaItems.toRxObservable()
            .concatMap { mediaItem ->
                val media = mediaMapper.toEntity(mediaItem)
                MakePath(media).build()
                    .flatMapCompletable { targetPath ->
                        val transferClient = FtpTransferClient(mediaItem.file, targetPath)
                        TransferFile(transferClient).transfer()
                    }
                    .andThen(Observable.just(mediaItem))
                    .doOnError { emitErrorMessage(it, mediaItem.file) }
                    .onErrorResumeNext(Observable.empty())
            }
            .subscribeOn(Schedulers.io())
            .observeOnFx()
            .buffer(Int.MAX_VALUE)
            .doFinally { isProcessing.set(false) }
            .subscribe {
                mediaItems.removeAll(it)
                updatedObservable.onNext(true)
                successfulUploadProperty.set(true)
            }
    }

    fun clearList() {
        updatedObservable.onNext(true)
        mediaItems.clear()
    }

    fun restrictedGroupings(item: MediaItem): List<Grouping> {
        val groupings = Grouping.values().toList()
        return when {
            item.isContainer -> {
                groupings.filter { it != Grouping.VERSE }
            }
            isChunkOrVerseFile(item.file) -> {
                val bttrChunk = BttrChunk()
                val wavMetadata = WavMetadata(listOf(bttrChunk))
                WavFile(item.file, wavMetadata)
                if (bttrChunk.metadata.mode == Grouping.CHUNK.grouping) {
                    groupings.filter { it != Grouping.CHUNK }
                } else {
                    groupings.filter { it != Grouping.VERSE }
                }
            }
            isChapterFile(item.file) -> {
                groupings.filter { it == Grouping.BOOK }
            }
            else -> listOf()
        }
    }

    private fun prepareFilesToImport(files: List<File>): List<File> {
        val filesToImport = mutableListOf<File>()
        files.forEach { file ->
            file.walk().filter { it.isFile }.forEach {
                filesToImport.add(it)
            }
        }
        return filesToImport
    }

    private fun importFiles(files: List<File>) {
        /*Observable.fromCallable {
            fileProcessRouter.handleFiles(files)
        }
            .subscribeOn(Schedulers.io())
            .observeOnFx()
            .doFinally { isProcessing.set(false) }
            .subscribe { resultList ->
                resultList.forEach {
                    if (it.status == FileStatus.REJECTED) {
                        emitErrorMessage(
                            message = messages["fileNotRecognized"],
                            fileName = it.requestedFile?.name ?: ""
                        )
                    } else {
                        val item = mediaMapper.fromEntity(it.data!!)
                        if (!mediaItems.contains(item)) mediaItems.add(item)
                    }
                }
                mediaItems.sort()
            }*/
    }

    private fun initThymeleafEngine() {
        thymeleafEngine.setTemplateResolver(ClassLoaderTemplateResolver().apply {
            prefix = "templates/"
            suffix = ".html"
            characterEncoding = "utf-8"
        })
    }

    private fun loadLanguages() {
        LanguagesReader().read()
            .subscribeOn(Schedulers.io())
            .observeOnFx()
            .subscribe { _languages ->
                languages.addAll(_languages)
            }
    }

    private fun loadResourceTypes() {
        ResourceTypesReader().read()
            .subscribeOn(Schedulers.io())
            .observeOnFx()
            .subscribe { _types ->
                resourceTypes.addAll(_types)
            }
    }

    private fun loadBooks() {
        BooksReader().read()
            .subscribeOn(Schedulers.io())
            .observeOnFx()
            .subscribe { _books ->
                books.addAll(_books)
            }
    }

    private fun loadVersification() {
        VersificationReader().read()
            .subscribeOn(Schedulers.io())
            .observeOnFx()
            .subscribe { _versification ->
                versification.set(_versification)
                fileVerifier = FileVerifier(versification.value)
            }
    }

    private fun isChunkOrVerseFile(file: File): Boolean {
        val chunkPattern = "_v[\\d]{1,3}(?:-[\\d]{1,3})?"
        val pattern = Pattern.compile(chunkPattern, Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(file.nameWithoutExtension)

        return matcher.find()
    }

    private fun isChapterFile(file: File): Boolean {
        val chapterPattern = "_c([\\d]{1,3})"
        val pattern = Pattern.compile(chapterPattern, Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(file.nameWithoutExtension)

        return matcher.find()
    }

    private fun emitErrorMessage(error: Throwable, file: File) {
        val notImportedText = MessageFormat.format(messages["notImported"], file.name)
        snackBarObservable.onNext("$notImportedText ${error.message ?: ""}")
    }

    private fun emitErrorMessage(message: String, fileName: String) {
        val notImportedText = MessageFormat.format(messages["notImported"], fileName)
        snackBarObservable.onNext("$notImportedText $message")
    }
}
