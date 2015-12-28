package com.github.rovkinmax.gallerypicker

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.github.rovkinmax.gallerypickerl.ImageUtil
import com.tbruyelle.rxpermissions.RxPermissions
import kotlinx.android.synthetic.main.activity_main.*
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.File

class MainActivity : AppCompatActivity() {
    val GALLERY_REQUEST_CODE = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button.setOnClickListener {
            requestGalleryPermission {
                val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
            }
        }
    }

    private fun requestGalleryPermission(function: (Boolean) -> Unit) {
        RxPermissions.getInstance(this)
                .request(Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe { function(it) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                GALLERY_REQUEST_CODE -> copyGalleryImageToTempFile(data)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun copyGalleryImageToTempFile(data: Intent?) {
        val galleryImagePath = getGalleryImagePath(data)

        if (galleryImagePath != null) {
            buildCopyObservable(galleryImagePath)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe { showLoading() }
                    .subscribe({ hideLoading(); showImage(it) }, { hideLoading(); it.printStackTrace() })

        }
    }


    private fun getGalleryImagePath(data: Intent?): String? {
        val uri = data?.data;
        var cursor: Cursor? = null
        try {
            cursor = contentResolver.query(uri, arrayOf(MediaStore.Images.Media.DATA), null, null, null);
            if (cursor != null) {
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) return cursor.getString(columnIndex);
            }
        } catch (e: Exception) {
        } finally {
            cursor?.close();
        }
        return null
    }

    private fun buildCopyObservable(imagePath: String): Observable<String> {
        return Observable.create<String> { sub ->
            val path = ImageUtil.copyExternalImageFile(applicationContext, imagePath)
            sub.onNext(path)
            sub.onCompleted()
        }
    }

    private fun hideLoading() {
        progress.visibility = View.INVISIBLE
    }

    private fun showLoading() {
        progress.visibility = View.VISIBLE
    }

    private fun showImage(path: String?) {
        imageView.setImageURI(Uri.fromFile(File(path)))
    }
}
