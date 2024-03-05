package org.bibletranslationtools.maui.jvm.mappers

import org.bibletranslationtools.maui.common.data.Media
import org.bibletranslationtools.maui.jvm.ui.MediaItem

class MediaMapper : IMapper<Media, MediaItem> {

    override fun fromEntity(type: Media): MediaItem {
        return MediaItem(type)
    }

    override fun toEntity(type: MediaItem): Media {
        return Media(
            type.file,
            type.language,
            type.resourceType,
            type.book,
            if (!type.chapter.isNullOrBlank()) type.chapter!!.toInt() else null,
            type.mediaExtension,
            type.mediaQuality,
            type.grouping
        )
    }
}
