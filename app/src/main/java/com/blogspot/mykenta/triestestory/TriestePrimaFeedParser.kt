package com.blogspot.mykenta.triestestory

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.util.*

class TriestePrimaFeedParser {
    // We don't use namespaces
    private val ns: String? = null

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(`in`: InputStream): List<News> {
        `in`.use { `in` ->
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(`in`, null)
            parser.nextTag()
            return readRss(parser)
        }
    }

    private fun readItem(parser: XmlPullParser): News {
        var title: String? = null
        var summary: String? = null
        var link: String? = null
        var category: String? = null
        var image: String? = null
        var html: String? = null

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            val name = parser.name
            when (name) {
                "title" -> title = readTitle(parser)
                "description" -> summary = readDescription(parser)
                "link" -> link = readText(parser)
                "category" -> category = readText(parser)
                "enclosure" -> image = readEnclosure(parser)
                "content:encoded" -> html = readText(parser)
                else -> skip(parser)
            }
        }
        return News(title, summary, link, category, image, html)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readChannel(parser: XmlPullParser): List<News> {
        val entries = ArrayList<News>()
        parser.require(XmlPullParser.START_TAG, ns, "channel")

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            val name = parser.name
            if (name == "item") {
                entries.add(readItem(parser))
            } else {
                skip(parser)
            }
        }
        return entries
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readTitle(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, ns, "title")
        val title = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, "title")
        return title
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readEnclosure(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, ns, "enclosure")
        val link = parser.getAttributeValue(null, "url")
        while (parser.next() != XmlPullParser.END_TAG) {

        }
        parser.require(XmlPullParser.END_TAG, ns, "enclosure")
        return link
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readDescription(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, ns, "description")
        val summary = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, "description")
        return summary
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readText(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return result
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readRss(parser: XmlPullParser): List<News> {
        parser.require(XmlPullParser.START_TAG, ns, "rss")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            val name = parser.name
            if (name == "channel") {
                return readChannel(parser)
            } else {
                skip(parser)
            }
        }

        return ArrayList()
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }
}