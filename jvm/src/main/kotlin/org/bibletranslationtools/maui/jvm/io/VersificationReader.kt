package org.bibletranslationtools.maui.jvm.io

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.reactivex.Single
import org.bibletranslationtools.maui.common.io.IVersificationReader
import org.bibletranslationtools.maui.common.io.Versification

class VersificationReader : IVersificationReader {
    @JsonIgnoreProperties(ignoreUnknown = true)
    private class VersificationSchema(
        val maxVerses: Map<String, List<Int>>
    )

    override fun read(): Single<Versification> {
        return Single.fromCallable {
            parseVersification()
        }
    }

    private fun parseVersification(): Versification {
        val versificationFile = javaClass.getResource("/eng.json").openStream()

        versificationFile.use { inputStream ->
            val versification: VersificationSchema = jacksonObjectMapper().readValue(inputStream)
            return versification.maxVerses
        }
    }
}