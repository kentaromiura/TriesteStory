package com.blogspot.mykenta.triestestory

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.common.Priority
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber
import com.facebook.imagepipeline.request.ImageRequest
import com.facebook.imagepipeline.request.ImageRequestBuilder
import java.util.*

class NewsAdapter(dataSet: ArrayList<News>, mContext: Context) :
        ArrayAdapter<News>(mContext, R.layout.notizia, dataSet), View.OnClickListener {

    // View lookup cache
    private class ViewHolder {
        internal var titolo: TextView? = null
        internal var descrizione: TextView? = null
        internal var immagine: SimpleDraweeView? = null
    }

    override fun onClick(v: View) {

    }

    private var lastPosition = -1

    @Suppress("NAME_SHADOWING", "DEPRECATION")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        // Get the data item for this position
        val dataModel = getItem(position)
        // Check if an existing view is being reused, otherwise inflate the view
        val viewHolder: ViewHolder // view lookup cache stored in tag

        if (convertView == null) {

            viewHolder = ViewHolder()
            val inflater = LayoutInflater.from(context)
            if (dataModel.html == null)
                convertView = inflater.inflate(R.layout.notizia_piccolo, parent, false)
            else
                convertView = inflater.inflate(R.layout.notizia, parent, false)
            viewHolder.titolo = convertView!!.findViewById(R.id.titolo_notizia) as TextView
            viewHolder.immagine = convertView.findViewById(R.id.immagine_notizia) as? SimpleDraweeView
            viewHolder.descrizione = convertView.findViewById(R.id.anteprima_notizia) as? TextView

            convertView.tag = viewHolder
        } else {
            viewHolder = convertView.tag as ViewHolder
        }

        lastPosition = position

        viewHolder.titolo?.text = dataModel.title
        if (dataModel.html == null) {
            viewHolder.titolo?.text = Html.fromHtml(
            "<b>" + dataModel.title + "</b>" + dataModel.summary,
            object: Html.ImageGetter {
                override fun getDrawable(url: String): Drawable {
                    val d = object : Drawable() {
                        override fun draw(canvas: Canvas?) {

                        }

                        override fun setAlpha(alpha: Int) {

                        }

                        override fun getOpacity(): Int {
                            return 0
                        }

                        override fun setColorFilter(colorFilter: ColorFilter?) {
                            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        }
                    }
                    // returns an empty image.
                    d.setBounds(0,0,0,0)
                    viewHolder.immagine?.setImageURI(url)
                    return d
                }
            }, null)
        } else {
            val uri = Uri.parse(dataModel!!.image)
            viewHolder.immagine?.setImageURI(uri)

            if (viewHolder.descrizione != null) {
                viewHolder.descrizione?.text = dataModel.summary
            }
        }

        // Return the completed view to render on screen
        return convertView
    }
}

