package com.example.threadingnetwork

import android.app.Activity

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle

import androidx.documentfile.provider.DocumentFile
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers.io
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private val scanFiles = PublishSubject.create<Uri>()
    private val compositeDisposable = CompositeDisposable()
    private var files = ""

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = "folderReader"

        read_button.setOnClickListener {
            openDirectory()
        }

        scanFiles
            .subscribeOn(io())
            .observeOn(io())
            .map {
                documentTree(it)
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                files += it
                files_list.text = files
            }
            .addTo(compositeDisposable)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun openDirectory() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(
        requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == 1
            && resultCode == Activity.RESULT_OK) {
            resultData?.data?.also { uri ->
                scanFiles.onNext(uri)
            }
        }
    }

    private fun documentTree(u: Uri): String {
        val documentsTree =  DocumentFile.fromTreeUri(application, u)
        val childDocuments = documentsTree?.listFiles()
        var string = ""
        childDocuments?.forEach {
            if (it.isDirectory) {
                string += "FOLDER: ${it.name}\n"
                scanFiles.onNext(it.uri)
            } else {
                string += "FILE: ${it.name}\n"
            }
        }
        return string
    }

}





