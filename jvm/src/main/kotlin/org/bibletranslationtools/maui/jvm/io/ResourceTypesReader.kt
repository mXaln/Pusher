package org.bibletranslationtools.maui.jvm.io

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.reactivex.Single
import org.bibletranslationtools.maui.common.io.IResourceReader

class ResourceTypesReader : IResourceReader {

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class ResourceTypeSchema(
        val slug: String
    )

    override fun read(): Single<List<String>> {
        return Single.fromCallable {
            parseResourceTypes()
        }
    }

    private fun parseResourceTypes(): List<String> {
        val resourceTypesFile = javaClass.getResource("/resource_types.json")?.openStream()

        resourceTypesFile?.use { inputStream ->
            val resourceTypes: List<ResourceTypeSchema> = jacksonObjectMapper().readValue(inputStream)

            return resourceTypes.map {
                it.slug
            }
        } ?: return listOf()
    }
}