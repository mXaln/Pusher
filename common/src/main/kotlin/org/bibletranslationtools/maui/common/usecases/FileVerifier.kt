package org.bibletranslationtools.maui.common.usecases

import org.bibletranslationtools.maui.common.data.Media
import org.bibletranslationtools.maui.common.data.FileStatus
import org.bibletranslationtools.maui.common.data.Grouping
import org.bibletranslationtools.maui.common.data.VerifiedResult
import org.bibletranslationtools.maui.common.io.Versification
import org.wycliffeassociates.otter.common.audio.wav.CueChunk
import org.wycliffeassociates.otter.common.audio.wav.WavFile
import org.wycliffeassociates.otter.common.audio.wav.WavMetadata

class FileVerifier(private val versification: Versification) {
    fun handleItem(media: Media): VerifiedResult {
        if (media.grouping == Grouping.CHAPTER) {
            isBookValid(media).let {
                if (it.status == FileStatus.ERROR) {
                    return it
                }
            }

            isChapterValid(media).let {
                if (it.status == FileStatus.ERROR) {
                    return it
                }
            }

            return isVerseValid(media)
        } else {
            return VerifiedResult(FileStatus.SUCCESS, media.file)
        }
    }

    private fun isBookValid(media: Media): VerifiedResult {
        val book = media.book?.uppercase()
        return if (book == null || !versification.contains(book)) {
            VerifiedResult(FileStatus.ERROR, media.file, "$book is not a valid book")
        } else {
            VerifiedResult(FileStatus.SUCCESS, media.file)
        }
    }

    private fun isChapterValid(media: Media): VerifiedResult {
        val book = media.book?.uppercase()
        val chapter = media.chapter

        if (chapter == null) {
            return VerifiedResult(FileStatus.ERROR, media.file, "$chapter is not a valid chapter")
        } else {
            versification[book]?.let { chapterVerses ->
                /** Check that chapter exists within book */
                val chapterNumber = chapter.toInt()

                if (chapterNumber > chapterVerses.size) {
                    return VerifiedResult(
                        FileStatus.ERROR,
                        media.file,
                        "$book only has ${chapterVerses.size} chapters, not $chapterNumber"
                    )
                } else {
                    return VerifiedResult(FileStatus.SUCCESS, media.file)
                }
            }
            return VerifiedResult(FileStatus.ERROR, media.file, "Book: $book does not exist")
        }
    }

    private fun isVerseValid(media: Media): VerifiedResult {
        val book = media.book?.uppercase()
        val chapter = media.chapter
        val cueChunk = CueChunk()
        val wavMetadata = WavMetadata(listOf(cueChunk))
        WavFile(media.file, wavMetadata)

        versification[book].let { chapterVerses ->
            val chapterNumber = chapter!!.toInt()
            val expectedVerses = chapterVerses?.get(chapterNumber - 1)
            val actualVerses = cueChunk.cues.size
            if (actualVerses != expectedVerses) {
                return VerifiedResult(
                    FileStatus.ERROR,
                    media.file,
                    "$book $chapter expected $expectedVerses verses, but got $actualVerses"
                )
            } else {
                return VerifiedResult(FileStatus.SUCCESS, media.file)
            }
        }
    }
}