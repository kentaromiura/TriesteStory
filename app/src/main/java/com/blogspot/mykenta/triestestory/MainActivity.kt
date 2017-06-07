package com.blogspot.mykenta.triestestory

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Window
import android.webkit.WebView
import com.facebook.drawee.backends.pipeline.Fresco
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import cz.msebera.android.httpclient.Header
import cz.msebera.android.httpclient.client.params.ClientPNames
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayInputStream
import java.text.SimpleDateFormat
import java.util.*
//import com.sun.xml.internal.ws.api.message.AddressingUtils.getFirstHeader
import cz.msebera.android.httpclient.HttpResponse
import cz.msebera.android.httpclient.impl.client.DefaultRedirectHandler
import cz.msebera.android.httpclient.protocol.HttpContext
import android.text.Html
import android.view.View
import android.widget.TextView
import android.view.ViewGroup
import android.widget.ArrayAdapter




class MainActivity : AppCompatActivity() {

    @Suppress("DEPRECATION")
    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fresco.initialize(this)
        setContentView(R.layout.activity_main)
        val context = this

        val timeStamp = SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().time)
        val uri = Uri.parse("http://www.osmer.fvg.it/previ/oggi_t.png?d=" + timeStamp)

        meteo_image.setImageURI(uri)
        val client = AsyncHttpClient()
        var piccoloclient = AsyncHttpClient()
        piccoloclient.setEnableRedirects(true)
        piccoloclient.get("http://ilpiccolo.gelocal.it/rss/home/", object : AsyncHttpResponseHandler() {
            override fun onFailure(statusCode: Int, headers: Array<Header>, errorResponse: ByteArray?, e: Throwable) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
            }

            override fun onSuccess(statusCode: Int, headers: Array<Header>, response: ByteArray?) {
                val triestePrimaParser = TriestePrimaFeedParser()
                val entries = triestePrimaParser.parse(ByteArrayInputStream(response)) as ArrayList<News>
                val adapter = NewsAdapter(
                        entries,
                        applicationContext
                )
                notizie_piccolo.adapter = adapter
                var isTablet = false
                if (meteo_text_description != null) {
                    isTablet = true
                }
                notizie_piccolo.setOnItemClickListener { _, _, position, _ ->
                    val dataModel = entries[position]
                    val dialog = Dialog(context)
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    dialog.setContentView(R.layout.dialog)
                    val webview = (dialog.findViewById(R.id.dialog_webview) as WebView)
                    webview.loadUrl("about:blank")
                    if (isTablet) {
                        webview.loadUrl(dataModel.link?.replace("ilpiccolo.gelocal", "m.ilpiccolo.gelocal"))
                    } else {
                        webview.loadData(
                                "<html><body><b>" + dataModel.title + "</b><br />" + dataModel.html + "<br /><a href=" + dataModel.link?.replace("ilpiccolo.gelocal", "m.ilpiccolo.gelocal") + ">Articolo completo</a></body></html>",
                                "text/html",
                                "UTF-8"
                        )
                    }

                    dialog.show()
                }

            }
        })
        client.get("http://triesteprima.it/rss", object : AsyncHttpResponseHandler() {

            override fun onSuccess(statusCode: Int, headers: Array<Header>, response: ByteArray) {
                // called when response HTTP status is "200 OK"
                val triestePrimaParser = TriestePrimaFeedParser()
                val entries = triestePrimaParser.parse(ByteArrayInputStream(response))
                val meteo = entries.find { it.category == "Meteo" }
                // check if tablet layout
                var isTablet = false
                if (meteo_text_description != null) {
                    isTablet = true
                }

                if (meteo != null) {
                    meteo_text.text = meteo?.title ?: ""
                    val onclick_listener = View.OnClickListener {
                        val dialog = Dialog(context)
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                        dialog.setContentView(R.layout.dialog)
                        val webview = dialog.findViewById(R.id.dialog_webview) as WebView

                        if (isTablet) {
                            webview.loadUrl("about:blank")
                            webview.loadUrl(meteo?.link)
                        } else {
                            webview.loadData(meteo?.html + "<br /><a href=" + meteo?.link + ">Articolo completo</a>", "text/html", "utf-8")
                        }
                        dialog.show()
                    }

                    if (isTablet) {
                        meteo_text_description.text = meteo?.summary ?: ""
                        meteo_text_description.setOnClickListener(onclick_listener)
                    }
                    meteo_text.setOnClickListener(onclick_listener)
                    meteo_image.setOnClickListener(onclick_listener)
                } else {
                    val osmerclient = AsyncHttpClient()
                    osmerclient.get("http://dev.meteo.fvg.it/xml/previsioni/PW" + timeStamp + ".xml", object : AsyncHttpResponseHandler() {
                        override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: ByteArray?) {
                            val osmerParser = OsmerParser()
                            val previsioni = osmerParser.parse(ByteArrayInputStream(response))
                            meteo_text.text = Html.fromHtml(previsioni.situazione_generale)
                            val onclick_listener = View.OnClickListener {
                                val dialog = Dialog(context)
                                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                                dialog.setContentView(R.layout.dialog)
                                val webview = dialog.findViewById(R.id.dialog_webview) as WebView
                                val html = "<b>"+ previsioni.situazione_generale + "</b><br />" + previsioni.tendenza
                                if (isTablet) {
                                    webview.loadUrl("about:blank")
                                    webview.loadUrl("http://m.meteo.fvg.it/home.php")
                                } else {
                                    webview.loadData(html + "<br /><a href=http://m.meteo.fvg.it/home.php>Articolo completo (OSMER) </a>", "text/html", "utf-8")
                                }
                                dialog.show()
                            }
                            meteo_text.setOnClickListener(onclick_listener)
                            meteo_image.setOnClickListener(onclick_listener)
                            if (isTablet){
                                meteo_text_description.text = Html.fromHtml(previsioni.tendenza)
                                meteo_text_description.setOnClickListener(onclick_listener)
                            }
                        }

                        override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseBody: ByteArray?, error: Throwable?) {

                        }
                    })

                    // http://dev.meteo.fvg.it/xml/previsioni/PW20170607.xml
                }



                val notizie_filtrate = entries.filter { it.category != "Meteo" } as ArrayList<News>
                val adapter = NewsAdapter(
                        notizie_filtrate,
                        applicationContext
                )
                notizie.adapter = adapter
                notizie.setOnItemClickListener { _, _, position, _ ->
                    val dataModel = notizie_filtrate[position]
                    val dialog = Dialog(context)
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    dialog.setContentView(R.layout.dialog)
                    val webview = (dialog.findViewById(R.id.dialog_webview) as WebView)
                    webview.loadUrl("about:blank")
                    if (isTablet) {
                        webview.loadUrl(dataModel.link)
                    } else {
                        webview.loadData(
                                "<html><body><b>" + dataModel.title + "</b><br />" + dataModel.html + "<br /><a href=" + dataModel.link + ">Articolo completo</a></body></html>",
                                "text/html",
                                "UTF-8"
                        )
                    }

                    dialog.show()
                }

            }

            override fun onFailure(statusCode: Int, headers: Array<Header>, errorResponse: ByteArray, e: Throwable) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
            }

        })


    }
}

