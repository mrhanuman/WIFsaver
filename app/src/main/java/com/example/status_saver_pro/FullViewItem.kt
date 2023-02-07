package com.example.status_saver_pro

import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import java.io.IOException
import java.io.OutputStream

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
            Glide.with(this@FullViewItem).load(uri) .into(imageView)

            btShare.setOnClickListener {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, Uri.parse(uri))
                    type = "image/jpg"
                }
                startActivity(Intent.createChooser(intent, "send image using : "))

            }

            btDownload.setOnClickListener {

                val dialog = Dialog(this@FullViewItem)
                dialog.setContentView(R.layout.custom_dialog_box)
                val btDownloadImage = dialog.findViewById<Button>(R.id.bt_download)
                dialog.show()
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                btDownloadImage.setOnClickListener {
                    dialog.dismiss()


                    val bitmap =
                        MediaStore.Images.Media.getBitmap(this.contentResolver, Uri.parse(uri))
                    val fileName = "${System.currentTimeMillis()}.jpg"
                    var fos: OutputStream?
                    contentResolver.also { resolver ->
                        val contentValues = ContentValues().apply {
                            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                            put(
                                MediaStore.MediaColumns.RELATIVE_PATH,
                                Environment.DIRECTORY_PICTURES
                            )
                        }
                        val imageUri: Uri? =
                            resolver.insert(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                contentValues
                            )
                        fos = imageUri?.let {
                            resolver.openOutputStream(it)
                        }

                    }


                    fos?.use {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                        Toast.makeText(applicationContext, "image saved !", Toast.LENGTH_SHORT)
                            .show()

                    }
                }
            }

        }

        else {
            imageView.visibility = View.GONE
            videoView.visibility = View.VISIBLE
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            videoView.setVideoPath(uri)
            videoView.setVideoURI(Uri.parse(uri))
            val mediaController = MediaController(this)
            videoView.setMediaController(mediaController)
            mediaController.setAnchorView(videoView)
            videoView.start()
            videoView.setOnCompletionListener {
                videoView.start()
                videoView.setOnCompletionListener {
                    this.finish()
                }


            }

            btShare.setOnClickListener {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, Uri.parse(uri))
                    type = "video/mp4"
                }
                startActivity(Intent.createChooser(intent, "send video using : "))
            }

            btDownload.setOnClickListener {
                val dialog = Dialog(this@FullViewItem)
                dialog.setContentView(R.layout.custom_dialog_box)
                val btDownloadDialog = dialog.findViewById<Button>(R.id.bt_download)
                dialog.show()
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                btDownloadDialog.setOnClickListener {

                    dialog.dismiss()
                    val inputStream = contentResolver.openInputStream(Uri.parse(uri))
                    val fileName = "${System.currentTimeMillis()}.mp4"

                    try {
                        val value = ContentValues()
                        value.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        value.put(MediaStore.MediaColumns.MIME_TYPE, "videos/mp4")
                        value.put(
                            MediaStore.MediaColumns.RELATIVE_PATH,
                            Environment.DIRECTORY_DOCUMENTS + "/videos/"
                        )

                        val uri =
                            contentResolver.insert(
                                MediaStore.Files.getContentUri("external"),
                                value
                            )
                        val outputStream: OutputStream =
                            uri?.let { contentResolver.openOutputStream(it) }!!
                        if (inputStream != null) {
                            outputStream.write(inputStream.readBytes())
                        }
                        outputStream.close()
                        Toast.makeText(applicationContext, "saved !", Toast.LENGTH_SHORT).show()
                    } catch (e: IOException) {
                        Toast.makeText(
                            applicationContext,
                            "something went wrong !",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
            }

        }


    }


}