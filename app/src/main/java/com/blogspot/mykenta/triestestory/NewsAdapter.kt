package com.blogspot.mykenta.triestestory

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView
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
            convertView = inflater.inflate(R.layout.notizia, parent, false)
            viewHolder.titolo = convertView!!.findViewById(R.id.titolo_notizia) as TextView
            viewHolder.immagine = convertView.findViewById(R.id.immagine_notizia) as SimpleDraweeView
            viewHolder.descrizione = convertView.findViewById(R.id.anteprima_notizia) as? TextView

            convertView.tag = viewHolder
        } else {
            viewHolder = convertView.tag as ViewHolder
        }

        lastPosition = position

        viewHolder.titolo?.text = dataModel.title
        val uri = Uri.parse(dataModel!!.image)
        viewHolder.immagine?.setImageURI(uri)

        if (viewHolder.descrizione != null) {
            viewHolder.descrizione?.text = dataModel.summary
        }
        // Return the completed view to render on screen
        return convertView
    }
}

