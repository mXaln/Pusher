package org.bibletranslationtools.maui.jvm.io

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.reactivex.Single
import org.bibletranslationtools.maui.common.io.IResourceReader

class BooksReader : IResourceReader {

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class BookSchema(
        val slug: String
    )

    override fun read(): Single<List<String>> {
        return Single.fromCallable {
            parseBooks()
        }
    }

    private fun parseBooks(): List<String> {
        val booksFile = javaClass.getResource("/book_catalog.json")?.openStream()

        booksFile?.use { inputStream ->
            val booksList: List<BookSchema> = jacksonObjectMapper().readValue(inputStream)

            return booksList.map {
                it.slug
            }
        } ?: return listOf()
    }
}
