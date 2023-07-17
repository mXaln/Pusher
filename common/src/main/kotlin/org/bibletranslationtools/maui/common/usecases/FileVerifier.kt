package org.bibletranslationtools.maui.common.usecases

import org.bibletranslationtools.maui.common.data.FileData
import org.bibletranslationtools.maui.common.data.FileStatus
import org.bibletranslationtools.maui.common.data.Grouping
import org.bibletranslationtools.maui.common.data.VerifiedResult
import org.bibletranslationtools.maui.common.io.Versification
import org.wycliffeassociates.otter.common.audio.wav.CueChunk
import org.wycliffeassociates.otter.common.audio.wav.WavFile
import org.wycliffeassociates.otter.common.audio.wav.WavMetadata

class FileVerifier(private val versification: Versification) {
    fun handleItem(fileData: FileData): VerifiedResult {
        if (fileData.grouping == Grouping.CHAPTER) {
            isBookValid(fileData).let {
                if (it.status == FileStatus.REJECTED) {
                    return it
                }
            }

            isChapterValid(fileData).let {
                if (it.status == FileStatus.REJECTED) {
                    return it
                }
            }

            return isVerseValid(fileData)
        } else {
            return VerifiedResult(FileStatus.PROCESSED, fileData.file)
        }
    }

    private fun isBookValid(fileData: FileData): VerifiedResult {
        val book = fileData.book?.uppercase()
        return if (book == null || !versification.contains(book)) {
            VerifiedResult(FileStatus.REJECTED, fileData.file, "$book is not a valid book")
        } else {
            VerifiedResult(FileStatus.PROCESSED, fileData.file)
        }
    }

    private fun isChapterValid(fileData: FileData): VerifiedResult {
        val book = fileData.book?.uppercase()
        val chapter = fileData.chapter

        if (chapter == null) {
            return VerifiedResult(FileStatus.REJECTED, fileData.file, "$chapter is not a valid chapter")
        } else {
            versification[book]?.let { chapterVerses ->
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
            return VerifiedResult(FileStatus.REJECTED, fileData.file, "Book: $book does not exist")
        }
    }

    private fun isVerseValid(fileData: FileData): VerifiedResult {
        val book = fileData.book?.uppercase()
        val chapter = fileData.chapter
        val cueChunk = CueChunk()
        val wavMetadata = WavMetadata(listOf(cueChunk))
        WavFile(fileData.file, wavMetadata)

        versification[book].let { chapterVerses ->
            val chapterNumber = chapter!!.toInt()
            val expectedVerses = chapterVerses?.get(chapterNumber - 1)
            val actualVerses = cueChunk.cues.size
            if (actualVerses != expectedVerses) {
                return VerifiedResult(
                    FileStatus.REJECTED,
                    fileData.file,
                    "$book $chapter expected $expectedVerses verses, but got $actualVerses"
                )
            } else {
                return VerifiedResult(FileStatus.PROCESSED, fileData.file)
            }
        }
    }
}