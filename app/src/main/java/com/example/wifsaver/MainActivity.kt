package com.example.wifsaver

import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.storage.StorageManager
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class MainActivity : AppCompatActivity() {
    private lateinit var rvStatusList: RecyclerView
    private lateinit var statusList: ArrayList<WhatsAppModel>
    private lateinit var statusAdapter: WhatsAppAdapter
    lateinit var swipeRefreshLayout: SwipeRefreshLayout

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        supportActionBar!!.title = "All Status"
        rvStatusList = findViewById(R.id.recycler_view)
        statusList = ArrayList()
        swipeRefreshLayout = findViewById(R.id.container)

        swipeRefreshLayout.setOnRefreshListener {

            swipeRefreshLayout.isRefreshing = true

            val sh = getSharedPreferences("DATA_PATH", MODE_PRIVATE)
            val uriPath = sh.getString("PATH", "")

            contentResolver.takePersistableUriPermission(
                Uri.parse(uriPath),
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            if (uriPath != null) {
                val docFile = DocumentFile.fromTreeUri(applicationContext, Uri.parse(uriPath))
                statusList.clear()
                for (file: DocumentFile in docFile!!.listFiles()) {

                    if (!file.name!!.endsWith(".nomedia")) {
                        val modelClass = WhatsAppModel(file.name!!, file.uri.toString())
                        statusList.add(modelClass)

                    }
                }
                setupRecyclerView(statusList)
            }
            swipeRefreshLayout.isRefreshing = false
            Toast.makeText(this, "status updated ", Toast.LENGTH_SHORT).show()



        }


        val result = readDataFromPrefs()
        if (result) {

            val sh = getSharedPreferences("DATA_PATH", MODE_PRIVATE)
            val uriPath = sh.getString("PATH", "")

            contentResolver.takePersistableUriPermission(
                Uri.parse(uriPath),
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            if (uriPath != null) {
                val docFile = DocumentFile.fromTreeUri(applicationContext, Uri.parse(uriPath))
                for (file: DocumentFile in docFile!!.listFiles()) {

                    if (!file.name!!.endsWith(".nomedia")) {
                        val modelClass = WhatsAppModel(file.name!!, file.uri.toString())
                        statusList.add(modelClass)

                    }
                }
                setupRecyclerView(statusList)
            }

        } else {
            getFolderPermission()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getFolderPermission() {
        val storageManager = application.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        val intent = storageManager.primaryStorageVolume.createOpenDocumentTreeIntent()
        val targetDirectory = "Android%2Fmedia%2Fcom.whatsapp%2FWhatsApp%2FMedia%2F.Statuses"
        var uri = intent.getParcelableExtra<Uri>("android.provider.extra.INITIAL_URI") as Uri
        var scheme = uri.toString()
        scheme = scheme.replace("/root/", "/tree/")
        scheme += "%3A$targetDirectory"
        uri = Uri.parse(scheme)
        intent.putExtra("android.provider.extra.INITIAL_URI", uri)
        intent.putExtra("android.content.extra.SHOW_ADVANCED", true)
        startActivityForResult(intent, 1234)


    }


    private fun readDataFromPrefs(): Boolean {
        val sh = getSharedPreferences("DATA_PATH", MODE_PRIVATE)
        val uriPath = sh.getString("PATH", "")
        if (uriPath != null) {
            if (uriPath.isEmpty()) {
                return false
            }
        }
        return true
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            val treeUri = data?.data

            val sharedPreferences = getSharedPreferences("DATA_PATH", MODE_PRIVATE)
            val myEdit = sharedPreferences.edit()
            myEdit.putString("PATH", treeUri.toString())
            myEdit.apply()

            if (treeUri != null) {
                contentResolver.takePersistableUriPermission(
                    treeUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                val docFile = DocumentFile.fromTreeUri(applicationContext, treeUri)
                for (file: DocumentFile in docFile!!.listFiles()) {

                    if (!file.name!!.endsWith(".nomedia")) {
                        val modelClass = WhatsAppModel(file.name!!, file.uri.toString())
                        statusList.add(modelClass)
                    }
                }
                setupRecyclerView(statusList)

            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun setupRecyclerView(statusList: java.util.ArrayList<WhatsAppModel>) {
        statusAdapter = applicationContext?.let {
            WhatsAppAdapter(it, statusList)
            { selectedStatusItem: WhatsAppModel ->
                listItemClicked(selectedStatusItem)
            }
        }!!

        rvStatusList.apply {
            setHasFixedSize(true)
            layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
            adapter = statusAdapter
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun listItemClicked(status: WhatsAppModel) {


        val dialog = Dialog(this@MainActivity)
        dialog.setContentView(R.layout.custom_dialog_box)
        val btDownload = dialog.findViewById<Button>(R.id.bt_download)
        dialog.show()
        btDownload.setOnClickListener {
            dialog.dismiss()
            saveFile(status)
        }
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveFile(status: WhatsAppModel) {
        if (status.fileUri.endsWith(".mp4")) {
            val inputStream = contentResolver.openInputStream(Uri.parse(status.fileUri))
            val fileName = "${System.currentTimeMillis()}.mp4"

            try {


                val value = ContentValues()
                value.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                value.put(MediaStore.MediaColumns.MIME_TYPE, "videos/mp4")
                value.put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_DOCUMENTS + "/videos/"
                )

                val uri = contentResolver.insert(MediaStore.Files.getContentUri("external"), value)
                val outputStream: OutputStream = uri?.let { contentResolver.openOutputStream(it) }!!
                if (inputStream != null) {
                    outputStream.write(inputStream.readBytes())
                }
                outputStream.close()
                Toast.makeText(applicationContext, "saved !", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                Toast.makeText(applicationContext, "something went wrong !", Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            val bitmap =
                MediaStore.Images.Media.getBitmap(this.contentResolver, Uri.parse(status.fileUri))
            val fileName = "${System.currentTimeMillis()}.jpg"
            var fos: OutputStream?
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentResolver.also { resolver ->
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                    }
                    val imageUri: Uri? =
                        resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    fos = imageUri?.let {
                        resolver.openOutputStream(it)
                    }

                }


            } else {
                val imageDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val image = File(imageDir, fileName)
                fos = FileOutputStream(image)
            }

            fos?.use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                Toast.makeText(applicationContext, "image saved !", Toast.LENGTH_SHORT).show()

            }
        }


    }
}
