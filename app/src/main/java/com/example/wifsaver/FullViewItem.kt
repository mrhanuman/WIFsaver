package com.example.wifsaver

import android.content.Intent
import android.media.MediaController2
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.MediaController
import android.widget.VideoView
import com.bumptech.glide.Glide

class FullViewItem : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_view_item)
        supportActionBar!!.hide()
        val imageView = findViewById<ImageView>(R.id.imageView)
        val videoView = findViewById<VideoView>(R.id.videoView)


        val uri = intent.getStringExtra("uri")
        if (uri!!.endsWith("jpg")) {
            imageView.visibility = View.VISIBLE
            videoView.visibility = View.GONE
            Glide.with(this@FullViewItem).load(uri).centerCrop().into(imageView)

        }else{
            imageView.visibility = View.GONE
            videoView.visibility = View.VISIBLE
            videoView.setVideoPath(uri)
            videoView.setVideoURI(Uri.parse(uri))
            val mediaController = MediaController(this)
            videoView.setMediaController(mediaController)
            videoView.start()
            videoView.setOnCompletionListener {

            }
        }
    }
}