package com.example.wifsaver

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.bumptech.glide.Glide

class FullViewItem : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_view_item)
        supportActionBar!!.hide()
        val imageView = findViewById<ImageView>(R.id.imageView)
        val videoView = findViewById<VideoView>(R.id.videoView)
        val btShare = findViewById<ImageView>(R.id.bt_share)
        val btDownload = findViewById<ImageView>(R.id.bt_download_full)


        val uri = intent.getStringExtra("uri")


        if (uri!!.endsWith("jpg")) {
            imageView.visibility = View.VISIBLE
            videoView.visibility = View.GONE
            Glide.with(this@FullViewItem).load(uri).centerCrop().into(imageView)
            btShare.setOnClickListener {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, Uri.parse(uri))
                    type = "image/jpg"
                }
                startActivity(Intent.createChooser(intent, "send image using : "))
            }

        }


        else {
            imageView.visibility = View.GONE
            videoView.visibility = View.VISIBLE
            videoView.setVideoPath(uri)

            videoView.setVideoURI(Uri.parse(uri))
            val mediaController = MediaController(this)
            videoView.setMediaController(mediaController)
            mediaController.setAnchorView(videoView)
            videoView.start()

            btShare.setOnClickListener {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, Uri.parse(uri))
                    type = "video/mp4"
                }
                startActivity(Intent.createChooser(intent, "send video using : "))
            }

        }


    }


}