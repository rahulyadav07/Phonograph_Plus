/*
 * Copyright (c) 2022 chr_56
 */

package player.phonograph.mediastore.sort

import android.provider.MediaStore.Audio
import java.lang.IllegalArgumentException
import kotlin.jvm.Throws

data class SortMode(val sortRef: SortRef, val revert: Boolean = false) {

    companion object {
        fun deserialize(str: String): SortMode {
            val array = str.split(':')
            return if (array.size != 2) SortMode(SortRef.ID) else
                SortMode(
                    SortRef.deserialize(array[0]), array[1] != "0"
                )
        }
    }

    fun serialize(): String =
        "${sortRef.serializedName}:${if (!revert) "0" else "1"}"

    @Suppress("PropertyName")
    val SQLQuerySortOrder: String
        get() {
            val first = when (sortRef) {
                SortRef.ID -> Audio.AudioColumns._ID
                SortRef.SONG_NAME -> Audio.Media.DEFAULT_SORT_ORDER
                SortRef.ARTIST_NAME -> Audio.Artists.DEFAULT_SORT_ORDER
                SortRef.ALBUM_NAME -> Audio.Albums.DEFAULT_SORT_ORDER
                SortRef.ADDED_DATE -> Audio.Media.DATE_ADDED
                SortRef.MODIFIED_DATE -> Audio.Media.DATE_MODIFIED
                SortRef.SONG_DURATION -> Audio.Media.DURATION
                SortRef.YEAR -> Audio.Media.YEAR
                SortRef.GENRE_NAME -> Audio.Genres.DEFAULT_SORT_ORDER // todo
                SortRef.SONG_COUNT -> "" // todo
                SortRef.ALBUM_COUNT -> "" // todo
            }
            val second = if (revert) "DESC" else "ASC"

            return "$first $second"
        }
}

enum class SortRef(val serializedName: String) {

    ID("id"),
    SONG_NAME("song_name"),
    ARTIST_NAME("artist_name"),
    ALBUM_NAME("number"),
    ADDED_DATE("added_date"),
    MODIFIED_DATE("modified_date"),
    SONG_COUNT("song_count"),
    ALBUM_COUNT("album_count"),
    SONG_DURATION("song_duration"),
    GENRE_NAME("genre_name"),
    YEAR("year");

    companion object {
        @Throws(IllegalArgumentException::class)
        fun deserialize(serializedName: String): SortRef {
            return when (serializedName) {
                "id" -> ID
                "song_name" -> SONG_NAME
                "artist_name" -> ARTIST_NAME
                "number" -> ALBUM_NAME
                "added_date" -> ADDED_DATE
                "modified_date" -> MODIFIED_DATE
                "song_count" -> SONG_COUNT
                "album_count" -> ALBUM_COUNT
                "song_duration" -> SONG_DURATION
                "genre_name" -> GENRE_NAME
                "year" -> YEAR
                else -> ID
            }
        }
    }
}
