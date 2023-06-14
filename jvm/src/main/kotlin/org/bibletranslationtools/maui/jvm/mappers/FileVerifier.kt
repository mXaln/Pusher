package org.bibletranslationtools.maui.jvm.mappers

import javafx.collections.ObservableMap
import org.bibletranslationtools.maui.common.data.FileStatus
import org.bibletranslationtools.maui.common.data.VerifiedResult
import org.bibletranslationtools.maui.jvm.ui.FileDataItem
import org.wycliffeassociates.otter.common.audio.wav.CueChunk
import org.wycliffeassociates.otter.common.audio.wav.WavFile
import org.wycliffeassociates.otter.common.audio.wav.WavMetadata
import tornadofx.isInt

class FileVerifier(private val versification: ObservableMap<String, List<Int>>) {
    fun handleItem(fileData: FileDataItem): VerifiedResult {

        bookExists(fileData).let {
            if (it.status == FileStatus.REJECTED) {
                return it
            }
        }

        chapterValid(fileData).let {
            if (it.status == FileStatus.REJECTED) {
                return it
            }
        }

        return verseValid(fileData)
    }

    private fun bookExists(fileData: FileDataItem): VerifiedResult {
        val book = fileData.book?.uppercase()
        return if (book == null || !versification.contains(book)) {
            VerifiedResult(FileStatus.REJECTED, fileData.file, "$book is not a valid book")
        } else {
            VerifiedResult(FileStatus.PROCESSED, fileData.file)
        }
    }

    private fun chapterValid(fileData: FileDataItem): VerifiedResult {
        val book = fileData.book?.uppercase()
        val chapter = fileData.chapter

        if (chapter == null || !chapter.isInt()) {
            return VerifiedResult(FileStatus.REJECTED, fileData.file, "$chapter is not a valid chapter")
        } else {
            versification[book]!!.let { chapterVerses ->
                /** Check that chapter exists within book */
                val chapterNumber = chapter.toInt()

                if (chapterNumber > chapterVerses.size) {
                    return VerifiedResult(
                        FileStatus.REJECTED,
                        fileData.file,
                        "$book only has ${chapterVerses.size} chapters, not $chapterNumber"
                    )
                } else {
                    return VerifiedResult(FileStatus.PROCESSED, fileData.file)
                }
            }
        }
    }

    private fun verseValid(fileData: FileDataItem): VerifiedResult {
        val book = fileData.book?.uppercase()
        val chapter = fileData.chapter
        val cueChunk = CueChunk()
        val wavMetadata = WavMetadata(listOf(cueChunk))
        WavFile(fileData.file, wavMetadata)

        versification[book]!!.let { chapterVerses ->
            val chapterNumber = chapter!!.toInt()
            val expected = chapterVerses[chapterNumber - 1]
            val actual = cueChunk.cues.size
            if (actual != expected) {
                return VerifiedResult(
                    FileStatus.REJECTED,
                    fileData.file,
                    "$book $chapter expected $expected verses, but got $actual"
                )
            } else {
                return VerifiedResult(FileStatus.PROCESSED, fileData.file)
            }
        }
    }
}