package com.blogspot.mykenta.triestestory

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream

class Previsione constructor(
        var situazione_generale: String?,
        var tendenza: String?
)
class OsmerParser {
    // We don't use namespaces
    private val ns: String? = null

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(`in`: InputStream): Previsione {
        `in`.use { `in` ->
            val parser = Xml.newPullParser()

            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(`in`, null)
            parser.nextTag()
            return readData(parser)
        }
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
    private fun readData(parser: XmlPullParser): Previsione {
        parser.require(XmlPullParser.START_TAG, ns, "data")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            val name = parser.name
            if (name == "previsioni") {
                return readPrevisioni(parser)
            } else {
                skip(parser)
            }
        }

        return Previsione(null, null)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readPrevisioni(parser: XmlPullParser): Previsione {
        val result = Previsione(null, null)
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            val name = parser.name
            if (name == "SITUAZIONEGENERALE_TESTO") {
                result.situazione_generale = readText(parser)
            } else if (name == "TENDENZA_TESTO") {
                result.tendenza = readText(parser)
            } else {
                skip(parser)
            }
        }
        return result
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