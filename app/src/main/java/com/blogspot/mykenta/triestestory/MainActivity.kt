package com.blogspot.mykenta.triestestory

import android.annotation.SuppressLint
import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Window
import android.webkit.WebView
import com.facebook.drawee.backends.pipeline.Fresco
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import cz.msebera.android.httpclient.Header
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayInputStream
import java.text.SimpleDateFormat
import java.util.*


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
        client.get("http://triesteprima.it/rss", object : AsyncHttpResponseHandler() {

            override fun onStart() {
                // called before request is started
            }

            override fun onSuccess(statusCode: Int, headers: Array<Header>, response: ByteArray) {
                // called when response HTTP status is "200 OK"
                val triestePrimaParser = TriestePrimaFeedParser()
                val entries = triestePrimaParser.parse(ByteArrayInputStream(response))
                val meteo = entries.find { it.category == "Meteo" }
                meteo_text.text = meteo?.title ?: ""
                var isTablet = false
                if (meteo_text_description != null) {
                    isTablet = true
                    meteo_text_description.text = meteo?.summary ?: ""
                    meteo_text_description.setOnClickListener {
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
                }

                meteo_text.setOnClickListener {
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
                meteo_image.setOnClickListener {
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

            override fun onRetry(retryNo: Int) {
                // called when request is retried
            }
        })


    }
}

