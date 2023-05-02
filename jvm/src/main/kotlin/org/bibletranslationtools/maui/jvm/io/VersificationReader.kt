package org.bibletranslationtools.maui.jvm.io

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.reactivex.Single
import org.bibletranslationtools.maui.common.io.IVersificationReader

class VersificationReader : IVersificationReader {
    @JsonIgnoreProperties(ignoreUnknown = true)
    private class VersificationSchema(
        val maxVerses: Map<String, List<Int>>
    )

    override fun read(): Single<Map<String, List<Int>>> {
        return Single.fromCallable {
            parseVersification()
        }
    }

    private fun parseVersification(): Map<String, List<Int>> {
        val versificationFile = javaClass.getResource("/eng.json").openStream()

        versificationFile.use { inputStream ->
            val versification: VersificationSchema = jacksonObjectMapper().readValue(inputStream)
            return versification.maxVerses
        }
    }
}