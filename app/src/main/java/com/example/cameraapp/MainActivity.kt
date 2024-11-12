package com.example.cameraapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var takeSelfieButton: Button
    private lateinit var sendSelfieButton: Button
    private var imageUri: Uri? = null

    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
        private const val CAMERA_REQUEST_CODE = 101
        private const val EMAIL_ADDRESS = "hodovychenko@op.edu.ua"
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)
        takeSelfieButton = findViewById(R.id.takeSelfieButton)
        sendSelfieButton = findViewById(R.id.sendSelfieButton)

        // Кнопка зробити селфі
        takeSelfieButton.setOnClickListener {
            if (checkCameraPermission()) {
                openCamera()
            } else {
                requestCameraPermission()
            }
        }

        // Кнопка надіслати селфі
        sendSelfieButton.setOnClickListener {
            sendEmailWithSelfie()
        }
    }

    // Перевірка дозволу на камеру
    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Запит дозволу на камеру
    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_CODE
        )
    }

    // Відкриття камери
    private fun openCamera() {
        val photoFile: File = createImageFile()
        imageUri = FileProvider.getUriForFile(this, "${packageName}.provider", photoFile)
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        }
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
    }

    // Створення файлу для збереження зображення
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("SELFIE_${timeStamp}_", ".jpg", storageDir)
    }

    // Обробка результату роботи камери
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            imageView.setImageURI(imageUri)
        }
    }

    // Надсилання селфі електронною поштою
    private fun sendEmailWithSelfie() {
        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(EMAIL_ADDRESS))
            putExtra(Intent.EXTRA_SUBJECT, "DigiJED [Ваше прізвище та ім'я]")
            putExtra(Intent.EXTRA_TEXT, "Додаю селфі для проекту DigiJED.\nРепозиторій GitHub: [Посилання на ваш репозиторій]")
            imageUri?.let { putExtra(Intent.EXTRA_STREAM, it) }
        }
        if (emailIntent.resolveActivity(packageManager) != null) {
            startActivity(Intent.createChooser(emailIntent, "Надіслати електронний лист"))
        } else {
            Toast.makeText(this, "Немає доступного додатку для надсилання електронної пошти", Toast.LENGTH_SHORT).show()
        }
    }

    // Обробка результату запиту дозволу
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            Toast.makeText(this, "Доступ до камери відхилено", Toast.LENGTH_SHORT).show()
        }
    }
}
