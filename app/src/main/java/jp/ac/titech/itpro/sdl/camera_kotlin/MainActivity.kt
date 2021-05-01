package jp.ac.titech.itpro.sdl.camera_kotlin

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    private val REQ_PHOTO: Int = 1234
    private var photoImage: Bitmap? = null
    private lateinit var photoView: ImageView
    private lateinit var currentPhotoPath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val photoButton: Button = findViewById(R.id.photo_button)
        photoButton.setOnClickListener {
            dispatchTakePictureIntent()
        }
        photoView = findViewById(R.id.photo_view)
    }

    private fun showPhoto(): Unit {
        if (photoImage == null) return

        photoView.setImageBitmap(photoImage)
    }

    private fun dispatchTakePictureIntent(): Unit {
        val takePictureIntent: Intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val packageManager: PackageManager = packageManager
        @SuppressLint("QueryPermissionsNeeded")
        val activities: List<ResolveInfo> = packageManager.queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY)
        if (activities.isNotEmpty()) {
            startActivityForResult(takePictureIntent, REQ_PHOTO)
        } else {
            Toast.makeText(this, R.string.toast_no_activities, Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(reqCode: Int, resCode: Int, data: Intent?): Unit {
        super.onActivityResult(reqCode, resCode, data)
        if (reqCode == REQ_PHOTO && resCode == RESULT_OK) {
            photoImage = data?.extras?.get("data") as Bitmap
        }
    }

    override fun onResume(): Unit {
        super.onResume()
        showPhoto()
    }
}