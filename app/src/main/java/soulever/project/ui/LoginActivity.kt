package soulever.project.ui

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import soulever.project.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    val TAG = "LoginActivity"
    private lateinit var loginActivityBinding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private var databaseReference: DatabaseReference? = null
    var database: FirebaseDatabase? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loginActivityBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(loginActivityBinding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseReference = database?.reference!!.child("Profile")


        val currentUser = auth.currentUser
/*        if (currentUser != null) {
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            finish()
        }*/


/*        loginActivityBinding.Email.doAfterTextChanged {
            val email = it.toString()
            RetrieveData(email, object : FireBaseCallback {
                override fun onCallback(email: String, password: String) {
                    Login(email, password)
                }
            })
        }*/

        loginActivityBinding.LoginButton.setOnClickListener {
            loginUser()
        }

        loginActivityBinding.register.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        cekUser()
    }
    private fun cekUser()
    {
        val user = auth.currentUser
        if (user != null)
        {
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            finish()
        }
    }
    private fun loginUser(){
        var email = loginActivityBinding.Email.text.toString()
        var password = loginActivityBinding.Password.text.toString()

        if (email.isNotEmpty() && password.isNotEmpty())
        {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    auth.signInWithEmailAndPassword(email,password).await()
                    cekUser()
                }catch (e : Exception){
                    e.message?.let { Log.d(TAG, it) }
                }
            }
        }
    }
    private fun Login(email: String, password: String) {
        loginActivityBinding.LoginButton.setOnClickListener {
            if (TextUtils.isEmpty(loginActivityBinding.Email.text.toString()) || !Patterns.EMAIL_ADDRESS.matcher(
                    loginActivityBinding.Email.text.toString()
                ).matches()
            ) {
                loginActivityBinding.Email.setError("Please enter your email")
                return@setOnClickListener
            } else if (TextUtils.isEmpty(loginActivityBinding.Password.text.toString())) {
                loginActivityBinding.Password.setError("Please enter your password")
                return@setOnClickListener
            } else if (loginActivityBinding.Email.text.toString() != email) {
                Toast.makeText(this@LoginActivity, "Email tidak tersedia", Toast.LENGTH_LONG).show()
            } else if (loginActivityBinding.Password.text.toString() != password) {
                Toast.makeText(this@LoginActivity, "Wrong Password", Toast.LENGTH_LONG).show()
            } else if (loginActivityBinding.Email.text.toString() != email && loginActivityBinding.Password.text.toString() == password) {
                Toast.makeText(this@LoginActivity, "Email anda salah", Toast.LENGTH_LONG).show()
            } else if (loginActivityBinding.Email.text.toString() != email && loginActivityBinding.Password.text.toString() == password) {
                Toast.makeText(this@LoginActivity, "Akun anda tidak tersedia", Toast.LENGTH_LONG)
                    .show()
            } else {
                auth.signInWithEmailAndPassword(
                    loginActivityBinding.Email.text.toString(),
                    loginActivityBinding.Password.text.toString()
                )
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(
                                this@LoginActivity,
                                "Login Failed, please try again!",
                                Toast.LENGTH_LONG
                            ).show()

                        }
                    }
            }
        }
    }

    private fun RetrieveData(email: String, callback: FireBaseCallback) {
        val currentUserDb = databaseReference?.orderByChild("Email")?.equalTo(email)
        currentUserDb?.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                if (snapshot.exists()) {
                    val email = snapshot.child("Email").value.toString()
                    val password = snapshot.child("Password").value.toString()
                    callback.onCallback(email, password)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    interface FireBaseCallback {
        fun onCallback(email: String, password: String)
    }
}