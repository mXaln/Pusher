package org.bibletranslationtools.maui.common.data

import java.lang.IllegalArgumentException

enum class Grouping(val grouping: String) {
    BOOK("book"),
    CHAPTER("chapter"),
    CHUNK("chunk"),
    VERSE("verse");

    companion object {
        fun of(grouping: String) =
            values().singleOrNull {
                it.name == grouping.uppercase() || it.grouping == grouping
            } ?: throw IllegalArgumentException("Grouping $grouping is not supported")
    }

    override fun toString(): String {
        return grouping
    }
}
