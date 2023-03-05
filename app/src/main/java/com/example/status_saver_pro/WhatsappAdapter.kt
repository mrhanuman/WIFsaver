package com.example.status_saver_pro

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class WhatsAppAdapter(private val context: Context, private var modelClass: ArrayList<WhatsAppModel>, private val clickListener : (WhatsAppModel)-> Unit) :
    RecyclerView.Adapter<WhatsAppAdapter.StatusViewHolder>() {


    class StatusViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivStatus = itemView.findViewById<ImageView>(R.id.iv_status)
        val ivVideoIcon = itemView.findViewById<ImageView>(R.id.iv_video_icon)
        val cvVideoCard = itemView.findViewById<CardView>(R.id.cv_video_card)
        val cvDownload = itemView.findViewById<CardView>(R.id.cv_download)
        val ivDownload = itemView.findViewById<ImageView>(R.id.iv_download)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatusViewHolder {
        return StatusViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.statuslist, parent, false)
        )
    }

    override fun onBindViewHolder(holder: StatusViewHolder, position: Int) {

        holder.cvDownload.visibility = View.VISIBLE
        holder.ivDownload.visibility = View.VISIBLE

        if (modelClass[position].fileUri.endsWith(".mp4")) {
            holder.cvVideoCard.visibility = View.VISIBLE
            holder.ivVideoIcon.visibility = View.VISIBLE
        } else {
            holder.cvVideoCard.visibility = View.GONE
            holder.ivVideoIcon.visibility = View.GONE
        }
       Glide.with(context).load((modelClass[position].fileUri)).into(holder.ivStatus)

        holder.ivDownload.setOnClickListener{
                clickListener(modelClass[position])
        }

            holder.ivStatus.setOnClickListener {
                    val intent = Intent(context, FullViewItem::class.java)
                    intent.putExtra("uri", modelClass[position].fileUri)
                    it.context.startActivity(intent)

            }

    }

    override fun getItemCount(): Int {
        return modelClass.size
    }
}
