package com.gamelaunch.frontend.data.parser

import android.util.Xml
import com.gamelaunch.frontend.data.db.entity.LaunchBoxGameEntity
import com.gamelaunch.frontend.data.db.entity.LaunchBoxImageEntity
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream

private const val BATCH_SIZE = 500

/**
 * Streams Metadata.xml from LaunchBox using XmlPullParser — the full ~1 GB uncompressed
 * file never lives in memory. Calls [onBatch] every BATCH_SIZE records to flush to Room.
 */
class LaunchBoxXmlParser {

    suspend fun parse(
        input: InputStream,
        onBatch: suspend (games: List<LaunchBoxGameEntity>, images: List<LaunchBoxImageEntity>) -> Unit,
        onProgress: suspend (gamesIndexed: Int) -> Unit = {}
    ) {
        val parser = Xml.newPullParser().apply {
            setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            setInput(input, "UTF-8")
        }

        val pendingGames = mutableListOf<LaunchBoxGameEntity>()
        val pendingImages = mutableListOf<LaunchBoxImageEntity>()
        var totalGames = 0

        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG) {
                when (parser.name) {
                    "Game"      -> parseGame(parser)?.let  { pendingGames.add(it);  totalGames++ }
                    "GameImage" -> parseImage(parser)?.let { pendingImages.add(it) }
                }
                if (pendingGames.size >= BATCH_SIZE) {
                    onBatch(pendingGames.toList(), pendingImages.toList())
                    onProgress(totalGames)
                    pendingGames.clear()
                    pendingImages.clear()
                }
            }
            event = parser.next()
        }

        if (pendingGames.isNotEmpty() || pendingImages.isNotEmpty()) {
            onBatch(pendingGames, pendingImages)
        }
    }

    private fun parseGame(parser: XmlPullParser): LaunchBoxGameEntity? {
        val f = readFields(parser, "Game")
        val id   = f["DatabaseID"]?.toIntOrNull() ?: return null
        val name = f["Name"]?.ifBlank { null }    ?: return null
        val plat = f["Platform"]?.ifBlank { null } ?: return null
        return LaunchBoxGameEntity(
            id          = id,
            name        = name,
            platform    = plat,
            releaseYear = f["ReleaseYear"]?.toIntOrNull(),
            overview    = f["Overview"]?.ifBlank { null },
            developer   = f["Developer"]?.ifBlank { null },
            publisher   = f["Publisher"]?.ifBlank { null },
            rating      = f["CommunityRating"]?.toFloatOrNull(),
            videoUrl    = f["VideoURL"]?.ifBlank { null }
        )
    }

    private fun parseImage(parser: XmlPullParser): LaunchBoxImageEntity? {
        val f      = readFields(parser, "GameImage")
        val gameId = f["DatabaseID"]?.toIntOrNull() ?: return null
        val file   = f["FileName"]?.ifBlank { null } ?: return null
        val type   = f["Type"]?.ifBlank { null }     ?: return null
        return LaunchBoxImageEntity(
            gameId   = gameId,
            fileName = file,
            type     = type,
            region   = f["Region"]?.ifBlank { null }
        )
    }

    /** Reads all direct child text values into a map, stopping at END_TAG for [rootTag]. */
    private fun readFields(parser: XmlPullParser, rootTag: String): Map<String, String> {
        val fields = mutableMapOf<String, String>()
        var event = parser.next()
        while (!(event == XmlPullParser.END_TAG && parser.name == rootTag)) {
            if (event == XmlPullParser.START_TAG) {
                val key = parser.name
                event = parser.next()
                if (event == XmlPullParser.TEXT) {
                    fields[key] = parser.text?.trim() ?: ""
                }
            }
            event = parser.next()
        }
        return fields
    }
}
