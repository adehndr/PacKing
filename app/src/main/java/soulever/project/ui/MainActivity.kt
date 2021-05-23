package soulever.project.ui

import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.google.cloud.storage.Storage
import soulever.project.R
import soulever.project.databinding.ActivityMainBinding
import soulever.project.ui.fragment.*
import soulever.project.utils.UploadImageHelper
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var currentPhotoPath : String
    private lateinit var imageName : String
    private var photoFile : File? = null
    private lateinit var permission : Array<String>

    companion object{
        const val REQUEST_IMAGE_CAPTURE = 1
        const val PERMISSION_REQ_CODE = 200
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        permission = arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.READ_PHONE_STATE,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )


        binding.fabCamera.setOnClickListener {
            permissionRequest()
            dispatchTakePictureIntent()
            readFromAsset()
        }



        makeCurrentFragment(HomeFragment())

        binding.bottomNavigation.background = null
        binding.bottomNavigation.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.page_1 -> makeCurrentFragment(HomeFragment())
                R.id.page_2 -> makeCurrentFragment(PesananFragment())
                R.id.page_3 -> makeCurrentFragment(NotifikasiFragment())
                R.id.page_4 -> makeCurrentFragment(ProfilFragment())
            }
            true
        }


    }

    private fun makeCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.container, fragment)
            addToBackStack(null)
            commit()
        }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.setting) {
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }

        return super.onOptionsItemSelected(item)
    }

    private fun dispatchTakePictureIntent(){
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            photoFile = createImageFile();
        }catch (ex: Exception){
            ex.printStackTrace()
        }

        if (photoFile != null) {
            val photoUri = FileProvider.getUriForFile(this, packageName, photoFile!!)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }

    }

    private fun createImageFile() : File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmSS").format(Date())
        val imageFileName = "JPEG_ $timeStamp _"
        val image = File.createTempFile(imageFileName, ".jpg")

        currentPhotoPath = image.absolutePath
        imageName = image.name

        return image

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val thread = Thread {
                try {
                    val storage: Storage? = UploadImageHelper.setCredentials(assets.open("GoogleMapDemo.json"))
                    UploadImageHelper.transmitImageFile(
                        storage as Storage,
                        currentPhotoPath,
                        "sampleImage.jpg"
                    )
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            thread.start()
            Log.e("TAG", "ImagePath: $currentPhotoPath")
            Log.e("TAG", "ImageName: $imageName")
        }

    }

    private fun permissionRequest(){
        if (ContextCompat.checkSelfPermission(applicationContext, permission[0]) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                applicationContext,
                permission[4]
            ) != PackageManager.PERMISSION_GRANTED){
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Camera Permission")
                .setMessage("Apakah anda ingn mengaktifkan kamera?")
                .setCancelable(true)
                .setPositiveButton("Ya", DialogInterface.OnClickListener { dialog, i ->
                    acceptPermissions()
                })
                .setNegativeButton("Tidak", DialogInterface.OnClickListener { dialog, i ->
                    finish()
                })

            builder.create().show()
        }
    }

    private fun acceptPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    permission.get(0)
                ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    applicationContext, permission.get(1)
                ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    applicationContext, permission.get(2)
                ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    applicationContext, permission.get(3)
                ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    applicationContext, permission.get(4)
                ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    applicationContext, permission.get(5)
                ) != PackageManager.PERMISSION_GRANTED){

                requestPermissions(permission, PERMISSION_REQ_CODE)
            }else {
                if (ContextCompat.checkSelfPermission(
                        applicationContext,
                        permission.get(0)
                    ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                        applicationContext, permission.get(1)
                    ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                        applicationContext, permission.get(2)
                    ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                        applicationContext, permission.get(3)
                    ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                        applicationContext, permission.get(4)
                    ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                        applicationContext, permission.get(5)
                    ) != PackageManager.PERMISSION_GRANTED){

                    requestPermissions(permission, PERMISSION_REQ_CODE)
                }
            }
        }


    }

    private fun readFromAsset(){
        var string = ""
        try {
            //InputStream inputStream = new FileInputStream(String.valueOf(getAssets().open("key.p12")));
            val inputStream = assets.open("GoogleMapDemo.json")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            string = String(buffer)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        Log.e("TAG", "ReadFromAsset: $string")
    }



}