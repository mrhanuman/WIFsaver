package com.example.status_saver_pro

import android.Manifest
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaScannerConnection
import android.media.MediaScannerConnection.MediaScannerConnectionClient
import android.net.Uri
import android.os.*
import android.os.Build.VERSION.SDK_INT
import android.os.storage.StorageManager
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.status_saver_pro.ads.Admob
import java.io.File
import java.io.IOException
import java.io.OutputStream


class MainActivity : AppCompatActivity() {
    private lateinit var rvStatusList: RecyclerView
    private lateinit var statusList: ArrayList<WhatsAppModel>
    private lateinit var statusAdapter: WhatsAppAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        Admob().setBanner(findViewById(R.id.main_activity_banner), this@MainActivity)

        supportActionBar!!.title = "All Status"
        rvStatusList = findViewById(R.id.recycler_view)
        statusList = ArrayList()
        swipeRefreshLayout = findViewById(R.id.container)

        swipeRefresh()
        requestPermission()

        if (SDK_INT < Build.VERSION_CODES.Q) {
            setupRecyclerView(statusList)
            getData()
        }
        else {

            setupRecyclerView(statusList)
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

    }


    private fun requestPermission() {
        if (SDK_INT < Build.VERSION_CODES.Q) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ), 100
            )
        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode==100){
            if (grantResults.isNotEmpty()){
                val write = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val read = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (read && write){
                    getData()
                }else{
                    Toast.makeText(this, "permission denied by user !", Toast.LENGTH_SHORT).show()
                    requestPermission()
                }
            }
        }
    }

    private fun checkPermission() {
        if (SDK_INT < Build.VERSION_CODES.Q) {
            val write =
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val read =
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED

        }
    }

    private fun getData() {
        val targetPath =
            Environment.getExternalStorageDirectory().absolutePath + "/WhatsApp/Media/.Statuses"
        val targetDir = File(targetPath)
        val files = targetDir.listFiles()
        if (files != null) {
            statusList.clear()
            for (file in files) {

                if (!file.name.endsWith(".nomedia")) {
                    statusList.add(WhatsAppModel(file.name, file.path))
                }

            }
        }
        setupRecyclerView(statusList)


    }


    private fun swipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener {

            swipeRefreshLayout.isRefreshing = true
            if (SDK_INT >= Build.VERSION_CODES.Q) {
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
            } else {
                getData()
            }
            swipeRefreshLayout.isRefreshing = false
            Toast.makeText(this, "status updated ", Toast.LENGTH_SHORT).show()


        }

    }


    private fun getFolderPermission() {
        if (SDK_INT >= Build.VERSION_CODES.Q) {
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

    private fun listItemClicked(status: WhatsAppModel) {


        val dialog = Dialog(this@MainActivity)
        dialog.setContentView(R.layout.custom_dialog_box)
        val btDownload = dialog.findViewById<Button>(R.id.bt_download)
        dialog.show()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        btDownload.setOnClickListener {
            dialog.dismiss()
            saveFile(status)
        }
    }


    private fun createFilesFolder() {
        if (!File("${Environment.getExternalStorageDirectory()}/Documents/StatusSaver/").exists()) {
            File("${Environment.getExternalStorageDirectory()}/Documents/StatusSaver/").mkdir()
        }
    }


    private fun saveFile(status: WhatsAppModel) {
        if (SDK_INT >= Build.VERSION_CODES.Q) {
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

                    val uri =
                        contentResolver.insert(MediaStore.Files.getContentUri("external"), value)
                    val outputStream: OutputStream =
                        uri?.let { contentResolver.openOutputStream(it) }!!
                    if (inputStream != null) {
                        outputStream.write(inputStream.readBytes())
                    }
                    outputStream.close()
                    Toast.makeText(applicationContext, "saved !", Toast.LENGTH_SHORT).show()
                } catch (e: IOException) {
                    Toast.makeText(applicationContext, "something went wrong !", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            else {
                val bitmap =
                    MediaStore.Images.Media.getBitmap(
                        this.contentResolver,
                        Uri.parse(status.fileUri)
                    )
                val fileName = "${System.currentTimeMillis()}.jpg"
                var fos: OutputStream?
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


                fos?.use {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                    Toast.makeText(applicationContext, "image saved !", Toast.LENGTH_SHORT).show()

                }
            }
        }
        else if (status.fileUri.endsWith(".mp4")) {
            try {
                createFilesFolder()
                val saveFilePath =
                    "${Environment.getExternalStorageDirectory()}/Documents/StatusSaver"
                val path: String = status.fileUri
                val fileName = path.substring(path.lastIndexOf("//") + 1)
                val file = File(path)
                val destFile = File(saveFilePath)
                try {
                    org.apache.commons.io.FileUtils.copyFileToDirectory(file, destFile)

                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
                val fileNameChange = "status_saver ${System.currentTimeMillis()}.mp4"
                val newFile = File(saveFilePath + fileNameChange)
                val contentType = "video/*"
                MediaScannerConnection.scanFile(applicationContext, arrayOf(newFile.absolutePath),
                    arrayOf(contentType), object : MediaScannerConnectionClient {
                        override fun onScanCompleted(p0: String?, p1: Uri?) {

                        }

                        override fun onMediaScannerConnected() {

                        }
                    }
                )
                val from = File(saveFilePath, fileName)
                val to = File(saveFilePath, fileNameChange)
                from.renameTo(to).apply {
                    Toast.makeText(applicationContext, "video saved", Toast.LENGTH_SHORT).show()
                }

            } catch (e: java.lang.Exception) {
                Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
            }
        } else {
            try {
                createFilesFolder()
                val saveFilePath =
                    "${Environment.getExternalStorageDirectory()}/Documents/StatusSaver"
                val path: String = status.fileUri
                val fileName = path.substring(path.lastIndexOf("//") + 1)
                val file = File(path)
                val destFile = File(saveFilePath)
                try {
                    org.apache.commons.io.FileUtils.copyFileToDirectory(file, destFile)

                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
                val fileNameChange = "status_saver ${System.currentTimeMillis()}.jpg"
                val newFile = File(saveFilePath + fileNameChange)
                val contentType = "image/*"
                MediaScannerConnection.scanFile(applicationContext, arrayOf(newFile.absolutePath),
                    arrayOf(contentType), object : MediaScannerConnectionClient {
                        override fun onScanCompleted(p0: String?, p1: Uri?) {

                        }

                        override fun onMediaScannerConnected() {

                        }
                    }
                )
                val from = File(saveFilePath, fileName)
                val to = File(saveFilePath, fileNameChange)
                from.renameTo(to).apply {
                    Toast.makeText(applicationContext, "image saved", Toast.LENGTH_SHORT).show()
                }

            } catch (e: java.lang.Exception) {
                Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
            }
        }


    }

    private fun dialogCall(message: String, btText: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.custom_dialog_box)
        val button = dialog.findViewById<Button>(R.id.bt_download)
        val textView = dialog.findViewById<TextView>(R.id.textView)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        button.text = btText
        textView.text = message
        dialog.show()
        button.setOnClickListener {
            dialog.dismiss()
            if (button.text.toString()=="exit"){
                this.finish()
            }
        }

    }


    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        dialogCall("To close the App Click exit", "exit")

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.exit -> dialogCall("To close the App Click exit", "exit")
            R.id.rate_us -> {
                dialogCall("Rate Us On Play Store", "PlayStore")
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
