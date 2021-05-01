package jp.ac.titech.itpro.sdl.camera_kotlin

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

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

    // Create a collision-resistant file
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
                "JPEG_${timeStamp}_",   /* prefix */
                ".jpg",                 /* suffix */
                storageDir                  /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    private fun dispatchTakePictureIntent(): Unit {
        val takePictureIntent: Intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val packageManager: PackageManager = packageManager
        @SuppressLint("QueryPermissionsNeeded")
        val activities: List<ResolveInfo> = packageManager.queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY)
        if (activities.isNotEmpty()) {
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                return
            }
            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "jp.ac.titech.itpro.sdl.fileprovider",
                        it
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQ_PHOTO)
            }
        } else {
            Toast.makeText(this, R.string.toast_no_activities, Toast.LENGTH_LONG).show()
        }
    }

    private fun setImage(): Unit {
        // Get the dimensions of the photoView
        val targetWidth: Int = photoView.width
        val targetHeight: Int = photoView.height

        val bmOptions = BitmapFactory.Options().apply {
            // Get the dimensions of the bitmap
            val photoWidth: Int = outWidth
            val photoHeight: Int = outHeight

            // Determine how much to scale down the image
            val scaleFactor: Int = Math.max(1, Math.min(photoWidth / targetWidth, photoHeight / targetHeight))

            // Decode the image file int o a Bitmap sized to fill the Views
            inJustDecodeBounds = false
            inSampleSize = scaleFactor
            inPurgeable = true
        }
        photoImage = BitmapFactory.decodeFile(currentPhotoPath, bmOptions)
    }

    override fun onActivityResult(reqCode: Int, resCode: Int, data: Intent?): Unit {
        super.onActivityResult(reqCode, resCode, data)
        if (reqCode == REQ_PHOTO && resCode == RESULT_OK) {
            setImage()
        }
    }

    override fun onResume(): Unit {
        super.onResume()
        showPhoto()
    }
}