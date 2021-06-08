package soulever.project.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.cloud.storage.Storage
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.json.JSONException
import org.json.JSONObject
import soulever.project.R
import soulever.project.adapter.ViewPagerAdapter
import soulever.project.databinding.ActivityMainBinding
import soulever.project.ui.fragment.*
import soulever.project.utils.LoadingDialog
import soulever.project.utils.UploadImageHelper
import soulever.project.utils.VolleySingleton
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.CoroutineContext


class MainActivity : AppCompatActivity(), CoroutineScope {
    private lateinit var binding: ActivityMainBinding
    private lateinit var currentPhotoPath: String
    private lateinit var imageName: String
    private var photoFile: File? = null
    private lateinit var permission: Array<String>
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private var loadingDialog: LoadingDialog? = null
    protected lateinit var job: Job

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
        const val PERMISSION_REQ_CODE = 200
    }

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadingDialog?.dismiss()

        permission = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        permissionRequest()


        binding.fabCamera.setOnClickListener {
            if (acceptPermissions()) {
                dispatchTakePictureIntent()
                readFromAsset()
            }
        }

        binding.bottomNavigation.background = null
        binding.viewPager.isUserInputEnabled = false
        viewPagerAdapter = ViewPagerAdapter(this)
        binding.viewPager.adapter = viewPagerAdapter
        binding.bottomNavigation.setOnNavigationItemSelectedListener(
            mOnNavigationItemSelectedListener
        )

        this.getSupportActionBar()?.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar()?.setCustomView(R.layout.action_bar);

    }


    private val mOnNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.page_1 -> {
                    binding.viewPager.setCurrentItem(0, false)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.page_2 -> {
                    binding.viewPager.setCurrentItem(1, false)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.page_3 -> {
                    binding.viewPager.setCurrentItem(2, false)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.page_4 -> {
                    binding.viewPager.setCurrentItem(3, false)
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
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

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            photoFile = createImageFile()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        if (photoFile != null) {
            val photoUri = FileProvider.getUriForFile(this, packageName, photoFile!!)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }

    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmSS").format(Date())
        val imageFileName = "JPEG_ $timeStamp _"
        val image = File.createTempFile(imageFileName, ".jpg")

        currentPhotoPath = image.absolutePath
        imageName = image.name

        return image

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        loadingDialog = LoadingDialog(this@MainActivity)
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            loadingDialog?.startLoading()

            val rootObject = JSONObject()
            rootObject.put("image_url", "https://amindfullmom.com/wp-content/uploads/2016/09/The-Best-Chicken-Soup.jpg")
            val urlApi = "https://us-central1-packing-314306.cloudfunctions.net/predict_pack"
            var responseObject = JSONObject()
            var arrayFood = ArrayList<String>()
            var idRekomendasi = ""
            val request = JsonObjectRequest(Request.Method.POST, urlApi, rootObject,
                { response ->
                    // Process the json
                    try {
                        responseObject = response
                        Log.d("APIisi", responseObject.toString())
                        idRekomendasi = responseObject.getString("food")
                        if (idRekomendasi == "") {
                            idRekomendasi = "rekomendasi-rendang"
                        } else {
                            idRekomendasi = "rekomendasi-" + idRekomendasi
                        }
                        Log.d("APIisi2", idRekomendasi)

                    } catch (e: Exception) {
                        e.message?.let { Log.d("APIisi", it) }
                    }
                }, {
                    // Error in request
                    Log.d("APIisi", it.toString() + " isi Error")
                })


            // Volley request policy, only one time request to avoid duplicate transaction
            request.retryPolicy = object : RetryPolicy {
                override fun getCurrentTimeout(): Int {
                    return 50000
                }

                override fun getCurrentRetryCount(): Int {
                    return 50000
                }

                @Throws(VolleyError::class)
                override fun retry(error: VolleyError) {
                }
            }

            // Add the volley post request to the request queue
            VolleySingleton.getInstance(this).addToRequestQueue(request)

            Handler(Looper.getMainLooper()).postDelayed({

                val thread = Thread {
                    try {
                        val storage: Storage? =
                            UploadImageHelper.setCredentials(assets.open("GoogleMapDemo.json"))
                        UploadImageHelper.transmitImageFile(
                            storage as Storage,
                            currentPhotoPath,
                            "tesDump.jpg"
                        )
                        val intent = Intent(this, RecommendedActivity::class.java)
                        intent.putExtra(RecommendedActivity.RECOMMENDED_ID, idRekomendasi)
                        startActivity(intent)

                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                thread.start()
            }, 5000)
            Log.e("TAG", "ImagePath: $currentPhotoPath")
            Log.e("TAG", "ImageName: $imageName")
        }
    }


    fun getIdRekomendasi() {
        val client = AsyncHttpClient()
        val urlApi = "https://us-central1-packing-314306.cloudfunctions.net/predict_pack"
        val rootObject = JSONObject()
        rootObject.put(
            "image_url",
            "https://img-global.cpcdn.com/recipes/e561f83c9c8c68b5/1200x630cq70/photo.jpg"
        )
        val params = RequestParams()
        params.put(
            "image_url",
            "https://img-global.cpcdn.com/recipes/e561f83c9c8c68b5/1200x630cq70/photo.jpg"
        )
        Looper.prepare()
        client.post(urlApi, params, object : JsonHttpResponseHandler() {
            override fun onSuccess(
                statusCode: Int,
                headers: Array<out Header>?,
                response: JSONObject?
            ) {
                super.onSuccess(statusCode, headers, response)
                val login: String = response?.getString("food") ?: ""
                Log.d("APIIsi", login)

            }
        })
    }


    private fun postDataUsingVolley(name: String) {
        // url to post our data
        val url = "https://us-central1-packing-314306.cloudfunctions.net/predict_pack"

        // creating a new variable for our request queue
        val queue = Volley.newRequestQueue(this@MainActivity)

        // on below line we are calling a string
        // request method to post the data to our API
        // in this we are calling a post method.
        val request: StringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener { response ->
                // inside on response method we are
                // hiding our progress bar
                // and setting data to edit text as empty

                // on below line we are displaying a success toast message.
                Toast.makeText(this@MainActivity, "Data added to API", Toast.LENGTH_SHORT).show()
                try {
                    // on below line we are passing our response
                    // to json object to extract data from it.
                    val respObj = JSONObject(response)

                    // below are the strings which we
                    // extract from our json object.
                    val name = respObj.getString("name")

                    // on below line we are setting this string s to our text view.
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error -> // method to handle errors.
                Toast.makeText(
                    this@MainActivity,
                    "Fail to get response = $error",
                    Toast.LENGTH_SHORT
                ).show()
            }) {
            override fun getParams(): Map<String, String>? {
                // below line we are creating a map for
                // storing our values in key and value pair.
                val params: MutableMap<String, String> = HashMap()

                // on below line we are passing our key
                // and value pair to our parameters.
                params["name"] = name
                // at last we are
                // returning our params.
                return params
            }
        }
        // below line is to make
        // a json object request.
        queue.add(request)
    }

    private fun permissionRequest(): Boolean {
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                permission.get(0)
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                applicationContext, permission.get(4)
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val builder1 = AlertDialog.Builder(this@MainActivity)
            builder1.setTitle("AAtten")
            builder1.setMessage("Permissions")
            builder1.setCancelable(false)
            builder1.setPositiveButton(
                "Yes"
            ) { dialog, which -> acceptPermissions() }
            builder1.setNegativeButton(
                "No"
            ) { dialog, which -> finish() }
            //Creating dialog box
            val alert1 = builder1.create()
            alert1.show()
        }

        return true

    }

    private fun acceptPermissions(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    applicationContext, permission[0]
                ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    applicationContext, permission[1]
                ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    applicationContext, permission[2]
                ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    applicationContext, permission[3]
                ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    applicationContext, permission[4]
                ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    applicationContext, permission[5]
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                requestPermissions(permission, PERMISSION_REQ_CODE)

            } else {
                if (ContextCompat.checkSelfPermission(
                        applicationContext,
                        permission[0]
                    ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                        applicationContext, permission[1]
                    ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                        applicationContext, permission[2]
                    ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                        applicationContext, permission[3]
                    ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                        applicationContext, permission[4]
                    ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                        applicationContext, permission[5]
                    ) != PackageManager.PERMISSION_GRANTED
                ) {

                    requestPermissions(permission, PERMISSION_REQ_CODE)
                }
            }
        }

        return true
    }

    private fun readFromAsset() {
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