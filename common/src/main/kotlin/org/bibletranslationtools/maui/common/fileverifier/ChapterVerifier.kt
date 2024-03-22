package org.bibletranslationtools.maui.common.fileverifier

import org.bibletranslationtools.maui.common.data.Grouping
import org.bibletranslationtools.maui.common.data.Media
import org.bibletranslationtools.maui.common.data.VerifiedResult
import org.bibletranslationtools.maui.common.extensions.MediaExtensions
import org.bibletranslationtools.maui.common.io.Versification

class ChapterVerifier(private val versification: Versification) : FileVerifier() {
    override fun verify(media: Media): VerifiedResult {
        val book = media.book?.uppercase()
        val chapter = media.chapter

        return when {
            media.chapter != null -> verifyChapter(book, chapter)
            media.grouping == Grouping.CHAPTER ->
                rejected("File with grouping ${Grouping.CHAPTER} must have chapter.")
            media.grouping == Grouping.CHUNK && media.extension != MediaExtensions.TR ->
                rejected("${media.extension} file with grouping ${Grouping.CHUNK} must have chapter.")
            media.grouping == Grouping.VERSE && media.extension != MediaExtensions.TR ->
                rejected("${media.extension} file with grouping ${Grouping.VERSE} must have chapter.")
            else -> processed()
        }
    }

    private fun verifyChapter(book: String?, chapter: Int?): VerifiedResult {
        return versification[book]?.let { chapterVerses ->
            /** Check that chapter exists within book */
            chapter?.let {
                val range = 1..chapterVerses.size

                if (!range.contains(chapter)) {
                    rejected("$book only has ${chapterVerses.size} chapters, not $chapter")
                } else {
                    processed()
                }
            }
        } ?: rejected("$chapter is not found in the book $book.")
    }
}