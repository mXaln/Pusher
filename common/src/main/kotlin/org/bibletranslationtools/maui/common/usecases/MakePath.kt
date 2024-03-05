package org.bibletranslationtools.maui.common.usecases

import io.reactivex.Single
import org.bibletranslationtools.maui.common.data.Media
import java.util.regex.Pattern

class MakePath(private val media: Media) {

    companion object {
        private const val CONTENTS = "CONTENTS"
    }

    private val filename = normalizeFileName()

    fun build(): Single<String> {
        return Single.fromCallable {
            validateMedia()

            val initialPath = buildInitialPath()

            when {
                media.isContainerAndCompressed -> {
                    arrayOf(
                        initialPath,
                        media.mediaExtension,
                        media.mediaQuality,
                        media.grouping,
                        filename
                    ).joinToString("/")
                }
                media.isContainer -> {
                    arrayOf(
                        initialPath,
                        media.mediaExtension,
                        media.grouping,
                        filename
                    ).joinToString("/")
                }
                media.isCompressed -> {
                    arrayOf(
                        initialPath,
                        media.mediaQuality,
                        media.grouping,
                        filename
                    ).joinToString("/")
                }
                else -> {
                    arrayOf(
                        initialPath,
                        media.grouping,
                        filename
                    ).joinToString("/")
                }
            }
        }
    }

    private fun validateMedia() {
        when {
            media.language.isNullOrBlank() -> {
                throw IllegalArgumentException("Language should be specified")
            }
            media.resourceType == null -> {
                throw IllegalArgumentException("Resource type should be specified")
            }
            media.chapter != null && media.book == null -> {
                throw IllegalArgumentException("Book needs to be specified")
            }
            media.grouping == null -> {
                throw IllegalArgumentException("Grouping needs to be specified")
            }
            (media.isContainer || media.isContainerAndCompressed) && media.mediaExtension == null -> {
                throw IllegalArgumentException("Media extension needs to be specified for container")
            }
            (media.isCompressed || media.isContainerAndCompressed) && media.mediaQuality == null -> {
                throw IllegalArgumentException("Media quality needs to be specified for compressed media")
            }
            !media.isContainer && media.mediaExtension != null -> {
                throw IllegalArgumentException("Media extension cannot be applied to non-container media")
            }
            !media.isCompressed && !media.isContainerAndCompressed && media.mediaQuality != null -> {
                throw IllegalArgumentException("Non-compressed media should not have a quality")
            }
        }
    }

    private fun buildInitialPath(): String {
        return when {
            media.book.isNullOrBlank() -> {
                arrayOf(
                    media.language,
                    media.resourceType,
                    CONTENTS,
                    media.extension
                ).joinToString("/")
            }
            media.chapter != null -> {
                arrayOf(
                    media.language,
                    media.resourceType,
                    media.book,
                    media.chapter,
                    CONTENTS,
                    media.extension
                ).joinToString("/")
            }
            else -> {
                arrayOf(
                    media.language,
                    media.resourceType,
                    media.book,
                    CONTENTS,
                    media.extension
                ).joinToString("/")
            }
        }
    }

    private fun normalizeFileName(): String {
        val filename = if (hasVerse()) {
            val filenameWithoutExtension = media.file.nameWithoutExtension
                .lowercase()

            filenameWithoutExtension
        } else {
            val str = StringBuilder()
            str.append("${media.language}_${media.resourceType}")

            if (!media.book.isNullOrBlank()) {
                str.append("_${media.book}")
            }

            if (media.chapter != null) {
                str.append("_c${media.chapter}")
            }

            str.toString()
        }

        return filename
            .replace(Regex("_t([\\d]{1,2})"), "")
            .plus(".${media.extension}")
    }

    private fun hasVerse(): Boolean {
        val verseRegex = "_(v[\\d]{1,3}(-[\\d]{1,3})?)"
        val pattern = Pattern.compile(verseRegex, Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(media.file.nameWithoutExtension)
        return matcher.find()
    }
}
