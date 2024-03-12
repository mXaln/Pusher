package org.bibletranslationtools.maui.common

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.reactivex.Single
import org.bibletranslationtools.maui.common.data.Batch
import org.bibletranslationtools.maui.common.data.Media
import org.bibletranslationtools.maui.common.persistence.IDirectoryProvider
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

class MediaSerializer : JsonSerializer<Lazy<List<Media>>>() {
    override fun serialize(lazyList: Lazy<List<Media>>, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartArray()
        lazyList.value.forEach { media ->
            gen.writeObject(media)
        }
        gen.writeEndArray()
    }
}

class MediaDeserializer : JsonDeserializer<Lazy<List<Media>>>() {
    override fun deserialize(parser: JsonParser, ctx: DeserializationContext): Lazy<List<Media>> {
        val reference = object : TypeReference<List<Media>>() {}
        val list = parser.readValueAs<List<Media>>(reference)

        return lazy {
            list
        }
    }
}

class BatchSerializer @Inject constructor(private val directoryProvider: IDirectoryProvider) {
    private val batchMapper = ObjectMapper().registerKotlinModule()

    fun createBatch(media: List<Media>): Single<Batch> {
        return Single.fromCallable {
            val filename = "${UUID.randomUUID()}.maui"

            val batch = Batch(
                directoryProvider.batchDirectory.resolve(filename),
                "Untitled batch",
                LocalDateTime.now().toString(),
                lazy { media }
            )

            if (!batch.file.exists()) {
                batch.file.createNewFile()
            }
            val jsonStr = batchMapper.writeValueAsString(batch)
            batch.file.writeText(jsonStr)

            batch
        }
    }

    fun getBatchList(): Single<List<Batch>> {
        return Single.fromCallable {
            directoryProvider.batchDirectory.listFiles()
                ?.filter { it.isFile && it.extension == "maui" }
                ?.map { file ->
                    val json = file.readText()
                    batchMapper.readValue(json)
                } ?: listOf()
        }
    }
}
