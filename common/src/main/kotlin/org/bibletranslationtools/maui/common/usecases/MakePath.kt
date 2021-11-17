package org.bibletranslationtools.maui.common.usecases

import io.reactivex.Single
import org.bibletranslationtools.maui.common.data.FileData
import java.util.regex.Pattern

class MakePath(private val fileData: FileData) {

    companion object {
        private const val CONTENTS = "CONTENTS"
    }

    private val filename = normalizeFileName()

    fun build(): Single<String> {
        return Single.fromCallable {
            validateFileData()

            val initialPath = buildInitialPath()

            when {
                fileData.isContainerAndCompressed -> {
                    arrayOf(
                        initialPath,
                        fileData.mediaExtension,
                        fileData.mediaQuality,
                        fileData.grouping,
                        filename
                    ).joinToString("/")
                }
                fileData.isContainer -> {
                    arrayOf(
                        initialPath,
                        fileData.mediaExtension,
                        fileData.grouping,
                        filename
                    ).joinToString("/")
                }
                fileData.isCompressed -> {
                    arrayOf(
                        initialPath,
                        fileData.mediaQuality,
                        fileData.grouping,
                        filename
                    ).joinToString("/")
                }
                else -> {
                    arrayOf(
                        initialPath,
                        fileData.grouping,
                        filename
                    ).joinToString("/")
                }
            }
        }
    }

    private fun validateFileData() {
        when {
            fileData.language.isNullOrBlank() -> {
                throw IllegalArgumentException("Language should be specified")
            }
            fileData.resourceType == null -> {
                throw IllegalArgumentException("Resource type should be specified")
            }
            fileData.chapter != null && fileData.book == null -> {
                throw IllegalArgumentException("Book needs to be specified")
            }
            fileData.grouping == null -> {
                throw IllegalArgumentException("Grouping needs to be specified")
            }
            (fileData.isContainer || fileData.isContainerAndCompressed) && fileData.mediaExtension == null -> {
                throw IllegalArgumentException("Media extension needs to be specified for container")
            }
            (fileData.isCompressed || fileData.isContainerAndCompressed) && fileData.mediaQuality == null -> {
                throw IllegalArgumentException("Media quality needs to be specified for compressed media")
            }
            !fileData.isContainer && fileData.mediaExtension != null -> {
                throw IllegalArgumentException("Media extension cannot be applied to non-container media")
            }
            !fileData.isCompressed && !fileData.isContainerAndCompressed && fileData.mediaQuality != null -> {
                throw IllegalArgumentException("Non-compressed media should not have a quality")
            }
        }
    }

    private fun buildInitialPath(): String {
        return when {
            fileData.book.isNullOrBlank() -> {
                arrayOf(
                    fileData.language,
                    fileData.resourceType,
                    CONTENTS,
                    fileData.extension
                ).joinToString("/")
            }
            fileData.chapter != null -> {
                arrayOf(
                    fileData.language,
                    fileData.resourceType,
                    fileData.book,
                    fileData.chapter,
                    CONTENTS,
                    fileData.extension
                ).joinToString("/")
            }
            else -> {
                arrayOf(
                    fileData.language,
                    fileData.resourceType,
                    fileData.book,
                    CONTENTS,
                    fileData.extension
                ).joinToString("/")
            }
        }
    }

    private fun normalizeFileName(): String {
        val filename = if (hasVerse()) {
            val filenameWithoutExtension = fileData.file.nameWithoutExtension
                .lowercase()

            filenameWithoutExtension
        } else {
            val str = StringBuilder()
            str.append("${fileData.language}_${fileData.resourceType}")

            if (!fileData.book.isNullOrBlank()) {
                str.append("_${fileData.book}")
            }

            if (fileData.chapter != null) {
                str.append("_c${fileData.chapter}")
            }

            str.toString()
        }

        return filename
            .replace(Regex("_t([\\d]{1,2})"), "")
            .plus(".${fileData.extension}")
    }

    private fun hasVerse(): Boolean {
        val verseRegex = "_(v[\\d]{1,3}(-[\\d]{1,3})?)"
        val pattern = Pattern.compile(verseRegex, Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(fileData.file.nameWithoutExtension)
        return matcher.find()
    }
}
