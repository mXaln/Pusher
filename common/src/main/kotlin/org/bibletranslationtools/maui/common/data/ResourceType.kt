package org.bibletranslationtools.maui.common.data

import java.lang.IllegalArgumentException

enum class ResourceType(val slug: String) {
    ULB("ulb"),
    AYT("ayt"),
    NAV("nav"),
    AVD("avd");

    companion object {
        fun of(slug: String) =
            values().singleOrNull {
                it.name == slug.uppercase() || it.slug == slug
            } ?: throw IllegalArgumentException("Resource type $slug is not supported")
    }

    override fun toString(): String {
        return slug
    }
}
